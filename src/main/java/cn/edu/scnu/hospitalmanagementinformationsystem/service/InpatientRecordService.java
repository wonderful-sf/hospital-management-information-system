package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.InpatientRecordCreateRequest;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Admission;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.InpatientRecord;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.InpatientRecordItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.InpatientRecordItemType;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Medicine;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.MedicineStatus;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrepaidRecord;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.AdmissionMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.InpatientRecordItemMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.InpatientRecordMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.MedicineMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.PrepaidRecordMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InpatientRecordService {

    private final InpatientRecordMapper inpatientRecordMapper;
    private final InpatientRecordItemMapper inpatientRecordItemMapper;
    private final AdmissionMapper admissionMapper;
    private final MedicineMapper medicineMapper;
    private final PrepaidRecordMapper prepaidRecordMapper;
    private final JdbcTemplate jdbcTemplate;
    private final Object recordNoLock = new Object();
    private static final DateTimeFormatter NO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public InpatientRecordService(InpatientRecordMapper inpatientRecordMapper,
                                   InpatientRecordItemMapper inpatientRecordItemMapper,
                                   AdmissionMapper admissionMapper,
                                   MedicineMapper medicineMapper,
                                   PrepaidRecordMapper prepaidRecordMapper,
                                   JdbcTemplate jdbcTemplate) {
        this.inpatientRecordMapper = inpatientRecordMapper;
        this.inpatientRecordItemMapper = inpatientRecordItemMapper;
        this.admissionMapper = admissionMapper;
        this.medicineMapper = medicineMapper;
        this.prepaidRecordMapper = prepaidRecordMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<InpatientRecord> findByAdmissionId(Long admissionId) {
        return inpatientRecordMapper.findByAdmissionId(admissionId);
    }

    public InpatientRecord findById(Long id) {
        InpatientRecord record = inpatientRecordMapper.findById(id);
        if (record == null) {
            throw new RuntimeException("住院记录不存在");
        }
        return record;
    }

    public List<InpatientRecordItem> findItemsByRecordId(Long recordId) {
        return inpatientRecordItemMapper.findByInpatientRecordId(recordId);
    }

    @Transactional
    public InpatientRecord create(InpatientRecordCreateRequest request) {
        if (request.admissionId() == null) {
            throw new RuntimeException("住院记录必须关联住院档案");
        }
        Admission admission = admissionMapper.findById(request.admissionId());
        if (admission == null) {
            throw new RuntimeException("住院档案不存在");
        }
        if (!"ACTIVE".equals(admission.status()) && !"SUSPENDED".equals(admission.status())) {
            throw new RuntimeException("仅活跃或欠费状态的住院档案可添加记录");
        }
        if (request.recordDate() == null) {
            throw new RuntimeException("记录日期不能为空");
        }
        InpatientRecord existing = inpatientRecordMapper.findByAdmissionIdAndDate(
            request.admissionId(), request.recordDate());
        if (existing != null) {
            throw new RuntimeException("同一天已存在住院记录，不可重复添加");
        }
        BigDecimal treatmentFee = request.treatmentFee() != null ? request.treatmentFee() : BigDecimal.ZERO;
        if (treatmentFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("诊疗费不能为负数");
        }
        BigDecimal itemsTotal = BigDecimal.ZERO;
        if (request.items() != null && !request.items().isEmpty()) {
            for (int i = 0; i < request.items().size(); i++) {
                final int idx = i;
                InpatientRecordItem item = request.items().get(i);
                if (item.itemType() == InpatientRecordItemType.MEDICINE) {
                    if (item.medicineId() == null) {
                        throw new RuntimeException("第" + (idx + 1) + "项药品未指定");
                    }
                    Medicine medicine = medicineMapper.findById(item.medicineId())
                        .orElseThrow(() -> new RuntimeException("第" + (idx + 1) + "项药品不存在"));
                    if (medicine.status() != MedicineStatus.ACTIVE) {
                        throw new RuntimeException("药品 " + medicine.name() + " 已停用");
                    }
                    if (medicine.stockQuantity() < item.quantity().intValue()) {
                        throw new RuntimeException("药品 " + medicine.name() + " 库存不足，当前库存: " + medicine.stockQuantity());
                    }
                    jdbcTemplate.update(
                        "UPDATE medicines SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?",
                        item.quantity().intValue(), medicine.id(), item.quantity().intValue());
                    itemsTotal = itemsTotal.add(medicine.unitPrice().multiply(item.quantity()));
                } else {
                    itemsTotal = itemsTotal.add(item.amount());
                }
            }
        }
        BigDecimal dailyCost = treatmentFee.add(itemsTotal);
        InpatientRecord record = new InpatientRecord(null, request.admissionId(), request.recordDate(),
            request.conditionDescription(), request.treatmentSummary(), treatmentFee, null);
        inpatientRecordMapper.insert(record);
        Long recordId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (request.items() != null && !request.items().isEmpty()) {
            for (InpatientRecordItem item : request.items()) {
                BigDecimal amount;
                String itemName;
                BigDecimal unitPrice;
                if (item.itemType() == InpatientRecordItemType.MEDICINE) {
                    Medicine medicine = medicineMapper.findById(item.medicineId()).orElseThrow();
                    amount = medicine.unitPrice().multiply(item.quantity());
                    itemName = medicine.name();
                    unitPrice = medicine.unitPrice();
                } else {
                    amount = item.amount();
                    itemName = item.itemName();
                    unitPrice = item.unitPrice();
                }
                InpatientRecordItem savedItem = new InpatientRecordItem(
                    null, recordId, item.itemType(), item.medicineId(),
                    itemName, unitPrice, item.quantity(), item.usageInstruction(), amount);
                inpatientRecordItemMapper.insert(savedItem);
            }
        }
        BigDecimal newBalance = admission.prepaidBalance().subtract(dailyCost);
        String recordNo = generateRecordNo();
        PrepaidRecord deduction = new PrepaidRecord(null, recordNo, request.admissionId(),
            admission.patientId(), dailyCost.negate(), "DEDUCTION", newBalance,
            "每日住院扣费", null);
        prepaidRecordMapper.insert(deduction);
        admissionMapper.updatePrepaidBalance(request.admissionId(), newBalance);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0 && "ACTIVE".equals(admission.status())) {
            admissionMapper.updateStatus(request.admissionId(), "SUSPENDED");
        }
        return new InpatientRecord(recordId, request.admissionId(), request.recordDate(),
            request.conditionDescription(), request.treatmentSummary(), treatmentFee, null);
    }

    private String generateRecordNo() {
        synchronized (recordNoLock) {
            String prefix = "PRE" + LocalDate.now().format(NO_DATE_FORMATTER);
            String maxNo = jdbcTemplate.queryForObject(
                "SELECT MAX(record_no) FROM prepaid_records WHERE record_no LIKE ?", String.class, prefix + "%");
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
