package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.AdmissionDto;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Admission;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Bed;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.BedStatus;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.InpatientRecord;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.InpatientRecordItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrepaidRecord;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Ward;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.AdmissionMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.BedMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.InpatientRecordItemMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.InpatientRecordMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.PrepaidRecordMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.WardMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdmissionService {
    private static final DateTimeFormatter NO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AdmissionMapper admissionMapper;
    private final InpatientRecordMapper inpatientRecordMapper;
    private final InpatientRecordItemMapper inpatientRecordItemMapper;
    private final PrepaidRecordMapper prepaidRecordMapper;
    private final BedMapper bedMapper;
    private final WardMapper wardMapper;
    private final JdbcTemplate jdbcTemplate;
    private final Object admissionNoLock = new Object();
    private final Object recordNoLock = new Object();

    public AdmissionService(AdmissionMapper admissionMapper,
                            InpatientRecordMapper inpatientRecordMapper,
                            InpatientRecordItemMapper inpatientRecordItemMapper,
                            PrepaidRecordMapper prepaidRecordMapper,
                            BedMapper bedMapper,
                            WardMapper wardMapper,
                            JdbcTemplate jdbcTemplate) {
        this.admissionMapper = admissionMapper;
        this.inpatientRecordMapper = inpatientRecordMapper;
        this.inpatientRecordItemMapper = inpatientRecordItemMapper;
        this.prepaidRecordMapper = prepaidRecordMapper;
        this.bedMapper = bedMapper;
        this.wardMapper = wardMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdmissionDto> findAll(Long patientId, Long doctorId) {
        return admissionMapper.findAllWithNames(patientId, doctorId);
    }

    public Admission findById(Long id) {
        Admission admission = admissionMapper.findById(id);
        if (admission == null) {
            throw new RuntimeException("住院档案不存在");
        }
        return admission;
    }

    @Transactional
    public Admission create(Long patientId, Long departmentId, Long attendingDoctorId,
                            Long bedId, BigDecimal initialDeposit) {
        if (patientId == null || departmentId == null || attendingDoctorId == null) {
            throw new RuntimeException("住院建档必须关联病人、科室和医生");
        }
        if (bedId != null) {
            Bed bed = bedMapper.findById(bedId).orElseThrow(() -> new RuntimeException("病床不存在"));
            if (bed.status() != BedStatus.AVAILABLE) {
                throw new RuntimeException("病床已被占用或维修中");
            }
        }
        String admissionNo = generateAdmissionNo();
        BigDecimal deposit = initialDeposit != null ? initialDeposit : BigDecimal.ZERO;
        if (deposit.compareTo(BigDecimal.valueOf(500)) < 0) {
            throw new RuntimeException("预缴金额不能低于 500 元");
        }
        Admission admission = new Admission(null, admissionNo, patientId, departmentId,
            attendingDoctorId, bedId, null, null, null, BigDecimal.ZERO, "ACTIVE");
        admissionMapper.insert(admission);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (bedId != null) {
            bedMapper.updateStatus(bedId, "OCCUPIED");
        }
        String recordNo = generateRecordNo();
        PrepaidRecord prepaid = new PrepaidRecord(null, recordNo, id, patientId,
            deposit, "DEPOSIT", deposit, "住院建档预缴", null);
        prepaidRecordMapper.insert(prepaid);
        admissionMapper.updatePrepaidBalance(id, deposit);
        return new Admission(id, admissionNo, patientId, departmentId, attendingDoctorId,
            bedId, LocalDateTime.now(), null, null, deposit, "ACTIVE");
    }

    @Transactional
    public PrepaidRecord deposit(Long admissionId, BigDecimal amount) {
        Admission admission = findById(admissionId);
        if (!"ACTIVE".equals(admission.status()) && !"SUSPENDED".equals(admission.status())) {
            throw new RuntimeException("仅活跃或欠费状态的住院记录可充值");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于 0");
        }
        BigDecimal newBalance = admission.prepaidBalance().add(amount);
        admissionMapper.updatePrepaidBalance(admissionId, newBalance);
        if ("SUSPENDED".equals(admission.status()) && newBalance.compareTo(BigDecimal.ZERO) >= 0) {
            admissionMapper.updateStatus(admissionId, "ACTIVE");
        }
        String recordNo = generateRecordNo();
        PrepaidRecord prepaid = new PrepaidRecord(null, recordNo, admissionId, admission.patientId(),
            amount, "DEPOSIT", newBalance, "预缴充值", null);
        prepaidRecordMapper.insert(prepaid);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return new PrepaidRecord(id, recordNo, admissionId, admission.patientId(),
            amount, "DEPOSIT", newBalance, "预缴充值", LocalDateTime.now());
    }

    @Transactional
    public void discharge(Long admissionId, String dischargeType) {
        Admission admission = findById(admissionId);
        if (!"ACTIVE".equals(admission.status()) && !"SUSPENDED".equals(admission.status())) {
            throw new RuntimeException("仅活跃或欠费状态的住院记录可出院");
        }
        if (dischargeType == null || dischargeType.isBlank()) {
            dischargeType = "NORMAL";
        }
        List<InpatientRecord> records = inpatientRecordMapper.findByAdmissionId(admissionId);
        BigDecimal totalCost = BigDecimal.ZERO;
        for (InpatientRecord record : records) {
            totalCost = totalCost.add(record.treatmentFee());
            List<InpatientRecordItem> items = inpatientRecordItemMapper.findByInpatientRecordId(record.id());
            for (InpatientRecordItem item : items) {
                totalCost = totalCost.add(item.amount());
            }
        }
        if (admission.bedId() != null) {
            Bed bed = bedMapper.findById(admission.bedId()).orElseThrow();
            Ward ward = wardMapper.findById(bed.wardId()).orElse(null);
            if (ward != null) {
                long days = ChronoUnit.DAYS.between(admission.admittedAt().toLocalDate(), LocalDate.now());
                if (days == 0) days = 1;
                totalCost = totalCost.add(ward.dailyCharge().multiply(BigDecimal.valueOf(days)));
            }
        }
        BigDecimal refundAmount = admission.prepaidBalance().subtract(totalCost);
        String billNo = generateBillNo();
        jdbcTemplate.update(
            "INSERT INTO bills (bill_no, patient_id, source_type, source_id, total_amount, status, paid_at, created_at, updated_at) VALUES (?, ?, 'INPATIENT', ?, ?, 'PAID', NOW(), NOW(), NOW())",
            billNo, admission.patientId(), admissionId, totalCost);
        Long billId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (admission.bedId() != null) {
            Bed bed = bedMapper.findById(admission.bedId()).orElseThrow();
            Ward ward = wardMapper.findById(bed.wardId()).orElse(null);
            if (ward != null) {
                long days = ChronoUnit.DAYS.between(admission.admittedAt().toLocalDate(), LocalDate.now());
                if (days == 0) days = 1;
                BigDecimal bedTotal = ward.dailyCharge().multiply(BigDecimal.valueOf(days));
                jdbcTemplate.update(
                    "INSERT INTO bill_items (bill_id, item_type, item_name, unit_price, quantity, amount) VALUES (?, 'BED', ?, ?, ?, ?)",
                    billId, ward.wardNo() + " " + bed.bedNo(), ward.dailyCharge(), BigDecimal.valueOf(days), bedTotal);
            }
        }
        for (InpatientRecord record : records) {
            jdbcTemplate.update(
                "INSERT INTO bill_items (bill_id, item_type, item_name, unit_price, quantity, amount) VALUES (?, 'TREATMENT', ?, ?, ?, ?)",
                billId, "诊疗费", record.treatmentFee(), BigDecimal.ONE, record.treatmentFee());
            List<InpatientRecordItem> items = inpatientRecordItemMapper.findByInpatientRecordId(record.id());
            for (InpatientRecordItem item : items) {
                jdbcTemplate.update(
                    "INSERT INTO bill_items (bill_id, item_type, item_name, unit_price, quantity, amount) VALUES (?, 'MEDICINE', ?, ?, ?, ?)",
                    billId, item.itemName(), item.unitPrice(), item.quantity(), item.amount());
            }
        }
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            String payNo = generatePaymentNo();
            jdbcTemplate.update(
                "INSERT INTO payments (payment_no, patient_id, bill_id, amount, method, paid_at, created_at) VALUES (?, ?, ?, ?, 'CASH', NOW(), NOW())",
                payNo, admission.patientId(), billId, refundAmount.negate());
            String recordNo = generateRecordNo();
            PrepaidRecord refund = new PrepaidRecord(null, recordNo, admissionId, admission.patientId(),
                refundAmount, "REFUND", BigDecimal.ZERO, "出院退款", null);
            prepaidRecordMapper.insert(refund);
        }
        admissionMapper.discharge(admissionId, LocalDateTime.now(), dischargeType, BigDecimal.ZERO);
        if (admission.bedId() != null) {
            bedMapper.updateStatus(admission.bedId(), "AVAILABLE");
        }
    }

    public List<PrepaidRecord> findPrepaidRecords(Long admissionId) {
        return prepaidRecordMapper.findByAdmissionId(admissionId);
    }

    private String generateAdmissionNo() {
        synchronized (admissionNoLock) {
            String prefix = "AD" + LocalDate.now().format(NO_DATE_FORMATTER);
            String maxNo = jdbcTemplate.queryForObject(
                "SELECT MAX(admission_no) FROM admissions WHERE admission_no LIKE ?", String.class, prefix + "%");
            int next;
            if (maxNo != null && maxNo.length() >= prefix.length()) {
                next = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
            } else {
                next = 1;
            }
            return prefix + String.format("%04d", next);
        }
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

    private String generateBillNo() {
        String prefix = "B" + LocalDate.now().format(NO_DATE_FORMATTER);
        String maxNo = jdbcTemplate.queryForObject(
            "SELECT MAX(bill_no) FROM bills WHERE bill_no LIKE ?", String.class, prefix + "%");
        int next;
        if (maxNo != null && maxNo.length() >= prefix.length()) {
            next = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
        } else {
            next = 1;
        }
        return prefix + String.format("%04d", next);
    }

    private String generatePaymentNo() {
        String prefix = "PAY" + LocalDate.now().format(NO_DATE_FORMATTER);
        String maxNo = jdbcTemplate.queryForObject(
            "SELECT MAX(payment_no) FROM payments WHERE payment_no LIKE ?", String.class, prefix + "%");
        int next;
        if (maxNo != null && maxNo.length() >= prefix.length()) {
            next = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
        } else {
            next = 1;
        }
        return prefix + String.format("%04d", next);
    }
}
