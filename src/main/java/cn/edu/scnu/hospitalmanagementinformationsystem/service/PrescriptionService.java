package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.PrescriptionUpdateRequest;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Medicine;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.MedicineStatus;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Prescription;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrescriptionItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.MedicineMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.OutpatientVisitMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.PrescriptionItemMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.PrescriptionMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrescriptionService {
    private static final DateTimeFormatter BILL_NO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final MedicineMapper medicineMapper;
    private final OutpatientVisitMapper outpatientVisitMapper;
    private final JdbcTemplate jdbcTemplate;
    private final Object billNoLock = new Object();

    public PrescriptionService(PrescriptionMapper prescriptionMapper,
                               PrescriptionItemMapper prescriptionItemMapper,
                               MedicineMapper medicineMapper,
                               OutpatientVisitMapper outpatientVisitMapper,
                               JdbcTemplate jdbcTemplate) {
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionItemMapper = prescriptionItemMapper;
        this.medicineMapper = medicineMapper;
        this.outpatientVisitMapper = outpatientVisitMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Prescription> findAll(Long patientId, Long doctorId) {
        return prescriptionMapper.findAll(patientId, doctorId);
    }

    public Prescription findById(Long id) {
        Prescription prescription = prescriptionMapper.findById(id);
        if (prescription == null) {
            throw new RuntimeException("处方不存在");
        }
        return prescription;
    }

    @Transactional
    public Prescription create(Prescription prescription, List<PrescriptionItem> items) {
        if (prescription.visitId() == null) {
            throw new RuntimeException("处方必须关联就诊记录");
        }
        var visit = outpatientVisitMapper.findById(prescription.visitId());
        if (visit == null) {
            throw new RuntimeException("就诊记录不存在");
        }
        if (prescriptionMapper.findByVisitId(prescription.visitId()) != null) {
            throw new RuntimeException("该就诊记录已开具处方");
        }

        BigDecimal consultationFee = prescription.consultationFee() != null ? prescription.consultationFee() : BigDecimal.ZERO;
        if (consultationFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("诊疗费不能为负数");
        }

        BigDecimal medicineAmount = BigDecimal.ZERO;
        List<PrescriptionItem> savedItems = new ArrayList<>();

        if (items != null) {
            for (PrescriptionItem item : items) {
                if (item.medicineId() == null) {
                    throw new RuntimeException("处方明细必须选择药品");
                }
                int quantity = item.quantity() != null ? item.quantity() : 0;
                if (quantity <= 0) {
                    throw new RuntimeException("药品数量必须大于 0");
                }
                Medicine medicine = medicineMapper.findById(item.medicineId())
                    .orElseThrow(() -> new RuntimeException("药品不存在: " + item.medicineId()));
                if (medicine.status() != MedicineStatus.ACTIVE) {
                    throw new RuntimeException("药品已停用: " + medicine.name());
                }
                if (medicine.stockQuantity() < quantity) {
                    throw new RuntimeException("药品库存不足: " + medicine.name());
                }

                BigDecimal unitPrice = medicine.unitPrice();
                BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
                medicineAmount = medicineAmount.add(amount);
                savedItems.add(new PrescriptionItem(null, null, medicine.id(), medicine.name(), unitPrice,
                    quantity, item.usageInstruction(), amount));

                Medicine updatedMedicine = new Medicine(medicine.id(), medicine.code(), medicine.name(),
                    medicine.specification(), medicine.unit(), medicine.unitPrice(),
                    medicine.stockQuantity() - quantity, medicine.status());
                medicineMapper.update(updatedMedicine);
            }
        }

        BigDecimal totalAmount = consultationFee.add(medicineAmount);
        Prescription toSave = new Prescription(null, visit.id(), visit.doctorId(), visit.patientId(),
            consultationFee, medicineAmount, totalAmount, "UNPAID", LocalDateTime.now());
        prescriptionMapper.insert(toSave);
        Long prescriptionId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        for (PrescriptionItem item : savedItems) {
            PrescriptionItem linked = new PrescriptionItem(null, prescriptionId, item.medicineId(),
                item.medicineName(), item.unitPrice(), item.quantity(), item.usageInstruction(), item.amount());
            prescriptionItemMapper.insert(linked);
        }

        createOutpatientBill(prescriptionId, visit.patientId(), consultationFee, savedItems, totalAmount);

        return new Prescription(prescriptionId, toSave.visitId(), toSave.doctorId(), toSave.patientId(),
            toSave.consultationFee(), toSave.medicineAmount(), toSave.totalAmount(), toSave.status(), toSave.createdAt());
    }

    @Transactional
    public Prescription update(Long id, PrescriptionUpdateRequest request) {
        Prescription existing = findById(id);
        if (!"UNPAID".equals(existing.status())) {
            throw new RuntimeException("已支付处方不可修改");
        }

        BigDecimal consultationFee = request.consultationFee() != null ? request.consultationFee() : BigDecimal.ZERO;
        if (consultationFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("诊疗费不能为负数");
        }

        List<PrescriptionItem> items = request.items();

        // Roll back old item stocks
        List<PrescriptionItem> oldItems = prescriptionItemMapper.findByPrescriptionId(id);
        for (PrescriptionItem oldItem : oldItems) {
            medicineMapper.findById(oldItem.medicineId()).ifPresent(medicine -> {
                Medicine restored = new Medicine(medicine.id(), medicine.code(), medicine.name(),
                    medicine.specification(), medicine.unit(), medicine.unitPrice(),
                    medicine.stockQuantity() + oldItem.quantity(), medicine.status());
                medicineMapper.update(restored);
            });
        }

        // Delete old items
        prescriptionItemMapper.deleteByPrescriptionId(id);

        // Validate and save new items
        BigDecimal medicineAmount = BigDecimal.ZERO;
        List<PrescriptionItem> savedItems = new ArrayList<>();

        if (items != null) {
            for (PrescriptionItem item : items) {
                if (item.medicineId() == null) {
                    throw new RuntimeException("处方明细必须选择药品");
                }
                int quantity = item.quantity() != null ? item.quantity() : 0;
                if (quantity <= 0) {
                    throw new RuntimeException("药品数量必须大于 0");
                }
                Medicine medicine = medicineMapper.findById(item.medicineId())
                    .orElseThrow(() -> new RuntimeException("药品不存在: " + item.medicineId()));
                if (medicine.status() != MedicineStatus.ACTIVE) {
                    throw new RuntimeException("药品已停用: " + medicine.name());
                }
                if (medicine.stockQuantity() < quantity) {
                    throw new RuntimeException("药品库存不足: " + medicine.name());
                }

                BigDecimal unitPrice = medicine.unitPrice();
                BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
                medicineAmount = medicineAmount.add(amount);
                savedItems.add(new PrescriptionItem(null, id, medicine.id(), medicine.name(), unitPrice,
                    quantity, item.usageInstruction(), amount));

                Medicine updatedMedicine = new Medicine(medicine.id(), medicine.code(), medicine.name(),
                    medicine.specification(), medicine.unit(), medicine.unitPrice(),
                    medicine.stockQuantity() - quantity, medicine.status());
                medicineMapper.update(updatedMedicine);
            }
        }

        BigDecimal totalAmount = consultationFee.add(medicineAmount);
        Prescription updated = new Prescription(id, existing.visitId(), existing.doctorId(), existing.patientId(),
            consultationFee, medicineAmount, totalAmount, existing.status(), existing.createdAt());
        prescriptionMapper.update(updated);

        // Insert new items
        for (PrescriptionItem item : savedItems) {
            prescriptionItemMapper.insert(item);
        }

        // Sync bill
        updateOutpatientBill(id, consultationFee, savedItems, totalAmount);

        return updated;
    }

    public void delete(Long id) {
        throw new RuntimeException("处方一旦创建不可删除");
    }

    private void createOutpatientBill(Long prescriptionId,
                                      Long patientId,
                                      BigDecimal consultationFee,
                                      List<PrescriptionItem> items,
                                      BigDecimal totalAmount) {
        String billNo = generateBillNo();
        jdbcTemplate.update(
            "INSERT INTO bills (bill_no, patient_id, source_type, source_id, total_amount, status, paid_at, created_at, updated_at) VALUES (?, ?, 'OUTPATIENT', ?, ?, 'UNPAID', NULL, NOW(), NOW())",
            billNo, patientId, prescriptionId, totalAmount);
        Long billId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        jdbcTemplate.update(
            "INSERT INTO bill_items (bill_id, item_type, item_name, unit_price, quantity, amount) VALUES (?, 'CONSULTATION', ?, ?, ?, ?)",
            billId, "门诊诊疗费", consultationFee, BigDecimal.ONE, consultationFee);

        for (PrescriptionItem item : items) {
            jdbcTemplate.update(
                "INSERT INTO bill_items (bill_id, item_type, item_name, unit_price, quantity, amount) VALUES (?, 'MEDICINE', ?, ?, ?, ?)",
                billId, item.medicineName(), item.unitPrice(), BigDecimal.valueOf(item.quantity()), item.amount());
        }
    }

    private void updateOutpatientBill(Long prescriptionId,
                                       BigDecimal consultationFee,
                                       List<PrescriptionItem> items,
                                       BigDecimal totalAmount) {
        Long billId = jdbcTemplate.queryForObject(
            "SELECT id FROM bills WHERE source_type = 'OUTPATIENT' AND source_id = ?",
            Long.class, prescriptionId);
        if (billId == null) return;

        jdbcTemplate.update(
            "UPDATE bills SET total_amount = ?, updated_at = NOW() WHERE id = ?",
            totalAmount, billId);

        jdbcTemplate.update("DELETE FROM bill_items WHERE bill_id = ?", billId);

        jdbcTemplate.update(
            "INSERT INTO bill_items (bill_id, item_type, item_name, unit_price, quantity, amount) VALUES (?, 'CONSULTATION', ?, ?, ?, ?)",
            billId, "门诊诊疗费", consultationFee, BigDecimal.ONE, consultationFee);

        for (PrescriptionItem item : items) {
            jdbcTemplate.update(
                "INSERT INTO bill_items (bill_id, item_type, item_name, unit_price, quantity, amount) VALUES (?, 'MEDICINE', ?, ?, ?, ?)",
                billId, item.medicineName(), item.unitPrice(), BigDecimal.valueOf(item.quantity()), item.amount());
        }
    }

    private String generateBillNo() {
        synchronized (billNoLock) {
            String prefix = "B" + LocalDate.now().format(BILL_NO_DATE_FORMATTER);
            String maxNo = jdbcTemplate.queryForObject(
                "SELECT MAX(bill_no) FROM bills WHERE bill_no LIKE ?",
                String.class,
                prefix + "%");
            int next;
            if (maxNo != null && maxNo.length() >= prefix.length()) {
                next = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
            } else {
                next = 1;
            }
            return prefix + String.format("%04d", next);
        }
    }
}
