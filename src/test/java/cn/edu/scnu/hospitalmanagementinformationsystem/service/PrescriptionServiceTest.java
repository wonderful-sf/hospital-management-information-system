package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.PrescriptionUpdateRequest;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Medicine;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.MedicineStatus;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.OutpatientVisit;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Prescription;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrescriptionItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.MedicineMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.OutpatientVisitMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.PrescriptionItemMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.PrescriptionMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionMapper prescriptionMapper;
    @Mock
    private PrescriptionItemMapper prescriptionItemMapper;
    @Mock
    private MedicineMapper medicineMapper;
    @Mock
    private OutpatientVisitMapper outpatientVisitMapper;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private PrescriptionService prescriptionService;

    private final LocalDateTime now = LocalDateTime.of(2026, 5, 16, 10, 0);

    @BeforeEach
    void setUp() {
        prescriptionService = new PrescriptionService(
            prescriptionMapper, prescriptionItemMapper, medicineMapper, outpatientVisitMapper, jdbcTemplate);
    }

    // ── create ──

    @Test
    @DisplayName("create throws when visit does not exist")
    void create_visitNotFound_throws() {
        var prescription = new Prescription(null, 1L, 1L, 1L, BigDecimal.ZERO, null, null, null, null);
        when(outpatientVisitMapper.findById(1L)).thenReturn(null);

        assertThatThrownBy(() -> prescriptionService.create(prescription, List.of()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("就诊记录不存在");
    }

    @Test
    @DisplayName("create throws when visit already has a prescription")
    void create_duplicateVisit_throws() {
        var visit = new OutpatientVisit(1L, 10L, 100L, 200L, "headache", "cold", now);
        var existingPrescription = new Prescription(5L, 1L, 200L, 100L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "UNPAID", now);
        when(outpatientVisitMapper.findById(1L)).thenReturn(visit);
        when(prescriptionMapper.findByVisitId(1L)).thenReturn(existingPrescription);

        var prescription = new Prescription(null, 1L, 1L, 1L, BigDecimal.ZERO, null, null, null, null);

        assertThatThrownBy(() -> prescriptionService.create(prescription, List.of()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("该就诊记录已开具处方");
    }

    @Test
    @DisplayName("create throws when medicine stock is insufficient")
    void create_insufficientStock_throws() {
        var visit = new OutpatientVisit(1L, 10L, 100L, 200L, "headache", "cold", now);
        var medicine = new Medicine(1L, "M001", "阿莫西林", "0.25g", "盒", new BigDecimal("15.00"), 3, MedicineStatus.ACTIVE);
        var item = new PrescriptionItem(null, null, 1L, null, null, 10, null, null);

        when(outpatientVisitMapper.findById(1L)).thenReturn(visit);
        when(prescriptionMapper.findByVisitId(1L)).thenReturn(null);
        when(medicineMapper.findById(1L)).thenReturn(Optional.of(medicine));

        var prescription = new Prescription(null, 1L, 1L, 1L, BigDecimal.ZERO, null, null, null, null);

        assertThatThrownBy(() -> prescriptionService.create(prescription, List.of(item)))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("药品库存不足");
    }

    @Test
    @DisplayName("create throws when medicine is disabled")
    void create_disabledMedicine_throws() {
        var visit = new OutpatientVisit(1L, 10L, 100L, 200L, "headache", "cold", now);
        var medicine = new Medicine(1L, "M001", "阿莫西林", "0.25g", "盒", new BigDecimal("15.00"), 100, MedicineStatus.DISABLED);
        var item = new PrescriptionItem(null, null, 1L, null, null, 1, null, null);

        when(outpatientVisitMapper.findById(1L)).thenReturn(visit);
        when(prescriptionMapper.findByVisitId(1L)).thenReturn(null);
        when(medicineMapper.findById(1L)).thenReturn(Optional.of(medicine));

        var prescription = new Prescription(null, 1L, 1L, 1L, BigDecimal.ZERO, null, null, null, null);

        assertThatThrownBy(() -> prescriptionService.create(prescription, List.of(item)))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("药品已停用");
    }

    @Test
    @DisplayName("create throws when consultation fee is negative")
    void create_negativeConsultationFee_throws() {
        var visit = new OutpatientVisit(1L, 10L, 100L, 200L, "headache", "cold", now);

        when(outpatientVisitMapper.findById(1L)).thenReturn(visit);
        when(prescriptionMapper.findByVisitId(1L)).thenReturn(null);

        var prescription = new Prescription(null, 1L, 1L, 1L, new BigDecimal("-5.00"), null, null, null, null);

        assertThatThrownBy(() -> prescriptionService.create(prescription, List.of()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("诊疗费不能为负数");
    }

    @Test
    @DisplayName("create deducts stock and returns prescription with calculated amounts")
    void create_valid_succeeds() {
        var visit = new OutpatientVisit(1L, 10L, 100L, 200L, "headache", "cold", now);
        var medicine = new Medicine(1L, "M001", "阿莫西林", "0.25g", "盒", new BigDecimal("15.00"), 10, MedicineStatus.ACTIVE);
        var item = new PrescriptionItem(null, null, 1L, null, null, 3, "饭后服用", null);

        when(outpatientVisitMapper.findById(1L)).thenReturn(visit);
        when(prescriptionMapper.findByVisitId(1L)).thenReturn(null);
        when(medicineMapper.findById(1L)).thenReturn(Optional.of(medicine));
        when(jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class)).thenReturn(100L);
        when(jdbcTemplate.queryForObject(
            "SELECT MAX(bill_no) FROM bills WHERE bill_no LIKE ?",
            String.class, "B" + LocalDate.now().toString().replace("-", "") + "%")).thenReturn(null);

        var prescription = new Prescription(null, 1L, 1L, 1L, new BigDecimal("20.00"), null, null, null, null);
        var result = prescriptionService.create(prescription, List.of(item));

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.patientId()).isEqualTo(100L);
        assertThat(result.doctorId()).isEqualTo(200L);
        assertThat(result.consultationFee()).isEqualByComparingTo("20.00");
        assertThat(result.medicineAmount()).isEqualByComparingTo("45.00");
        assertThat(result.totalAmount()).isEqualByComparingTo("65.00");
        assertThat(result.status()).isEqualTo("UNPAID");

        verify(medicineMapper).update(any(Medicine.class));
        verify(prescriptionMapper).insert(any(Prescription.class));
        verify(prescriptionItemMapper).insert(any(PrescriptionItem.class));
    }

    // ── update ──

    @Test
    @DisplayName("update rolls back old stock and deducts new stock")
    void update_rollsBackAndDeductsStock() {
        var existing = new Prescription(1L, 10L, 200L, 100L, new BigDecimal("20.00"), new BigDecimal("45.00"), new BigDecimal("65.00"), "UNPAID", now);
        var oldItem = new PrescriptionItem(1L, 1L, 1L, "阿莫西林", new BigDecimal("15.00"), 3, "饭后服用", new BigDecimal("45.00"));
        var medicine = new Medicine(1L, "M001", "阿莫西林", "0.25g", "盒", new BigDecimal("15.00"), 10, MedicineStatus.ACTIVE);

        when(prescriptionMapper.findById(1L)).thenReturn(existing);
        when(prescriptionItemMapper.findByPrescriptionId(1L)).thenReturn(List.of(oldItem));
        when(medicineMapper.findById(1L))
            .thenReturn(Optional.of(medicine))
            .thenReturn(Optional.of(new Medicine(1L, "M001", "阿莫西林", "0.25g", "盒", new BigDecimal("15.00"), 13, MedicineStatus.ACTIVE)));

        var newItem = new PrescriptionItem(null, null, 1L, null, null, 2, "每日三次", null);
        var request = new PrescriptionUpdateRequest(new BigDecimal("20.00"), List.of(newItem));

        var result = prescriptionService.update(1L, request);

        assertThat(result.medicineAmount()).isEqualByComparingTo("30.00");
        assertThat(result.totalAmount()).isEqualByComparingTo("50.00");

        verify(medicineMapper).update(new Medicine(1L, "M001", "阿莫西林", "0.25g", "盒", new BigDecimal("15.00"), 13, MedicineStatus.ACTIVE));
        verify(medicineMapper).update(new Medicine(1L, "M001", "阿莫西林", "0.25g", "盒", new BigDecimal("15.00"), 11, MedicineStatus.ACTIVE));

        verify(prescriptionItemMapper).deleteByPrescriptionId(1L);
        verify(prescriptionMapper).update(any(Prescription.class));
    }

    @Test
    @DisplayName("update throws when prescription is already PAID")
    void update_paidPrescription_throws() {
        var existing = new Prescription(1L, 10L, 200L, 100L, new BigDecimal("20.00"), new BigDecimal("45.00"), new BigDecimal("65.00"), "PAID", now);
        when(prescriptionMapper.findById(1L)).thenReturn(existing);

        var request = new PrescriptionUpdateRequest(new BigDecimal("20.00"), List.of());

        assertThatThrownBy(() -> prescriptionService.update(1L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("已支付处方不可修改");
    }

    @Test
    @DisplayName("update throws when medicine stock insufficient after rollback")
    void update_newItemInsufficientStock_throws() {
        var existing = new Prescription(1L, 10L, 200L, 100L, new BigDecimal("20.00"), new BigDecimal("45.00"), new BigDecimal("65.00"), "UNPAID", now);
        var oldItem = new PrescriptionItem(1L, 1L, 1L, "阿莫西林", new BigDecimal("15.00"), 3, "饭后服用", new BigDecimal("45.00"));
        var medicine = new Medicine(1L, "M001", "阿莫西林", "0.25g", "盒", new BigDecimal("15.00"), 10, MedicineStatus.ACTIVE);

        when(prescriptionMapper.findById(1L)).thenReturn(existing);
        when(prescriptionItemMapper.findByPrescriptionId(1L)).thenReturn(List.of(oldItem));
        when(medicineMapper.findById(1L))
            .thenReturn(Optional.of(medicine))
            .thenReturn(Optional.of(new Medicine(1L, "M001", "阿莫西林", "0.25g", "盒", new BigDecimal("15.00"), 1, MedicineStatus.ACTIVE)));

        var newItem = new PrescriptionItem(null, null, 1L, null, null, 10, "每日三次", null);
        var request = new PrescriptionUpdateRequest(new BigDecimal("20.00"), List.of(newItem));

        assertThatThrownBy(() -> prescriptionService.update(1L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("药品库存不足");
    }

    // ── delete ──

    @Test
    @DisplayName("delete always throws (prescriptions are immutable)")
    void delete_alwaysThrows() {
        assertThatThrownBy(() -> prescriptionService.delete(1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("处方一旦创建不可删除");
    }

    // ── findById ──

    @Test
    @DisplayName("findById throws when prescription not found")
    void findById_notFound_throws() {
        when(prescriptionMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> prescriptionService.findById(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("处方不存在");
    }
}
