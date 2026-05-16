package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Registration;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.RegistrationMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationMapper registrationMapper;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(registrationMapper, jdbcTemplate);
    }

    // ── create ──

    @Test
    @DisplayName("create throws when patientId is missing")
    void create_missingPatientId_throws() {
        var reg = new Registration(null, null, 1L, 1L, 1L, "普通门诊", null, null);

        assertThatThrownBy(() -> registrationService.create(reg))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("挂号必须关联病人、医生和科室");
    }

    @Test
    @DisplayName("create throws when doctorId is missing")
    void create_missingDoctorId_throws() {
        var reg = new Registration(null, 1L, null, 1L, 1L, "普通门诊", null, null);

        assertThatThrownBy(() -> registrationService.create(reg))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("挂号必须关联病人、医生和科室");
    }

    @Test
    @DisplayName("create throws when visitType is blank")
    void create_blankVisitType_throws() {
        var reg = new Registration(null, 1L, 1L, 1L, 1L, "  ", null, null);

        assertThatThrownBy(() -> registrationService.create(reg))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("就诊类型不能为空");
    }

    @Test
    @DisplayName("create succeeds with valid registration")
    void create_valid_succeeds() {
        var reg = new Registration(null, 1L, 2L, 3L, 4L, "急诊", null, null);
        when(jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class)).thenReturn(100L);

        var result = registrationService.create(reg);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.patientId()).isEqualTo(1L);
        assertThat(result.doctorId()).isEqualTo(2L);
        assertThat(result.departmentId()).isEqualTo(3L);
        assertThat(result.scheduleId()).isEqualTo(4L);
        assertThat(result.visitType()).isEqualTo("急诊");
        assertThat(result.status()).isEqualTo("REGISTERED");

        verify(registrationMapper).insert(any(Registration.class));
    }

    // ── update ──

    @Test
    @DisplayName("update only allowed when status is REGISTERED")
    void update_visitedRegistration_throws() {
        var existing = new Registration(1L, 1L, 2L, 3L, 4L, "普通门诊", LocalDateTime.now(), "VISITED");
        when(registrationMapper.findById(1L)).thenReturn(existing);

        var update = new Registration(null, 1L, 2L, 3L, 4L, "急诊", null, null);

        assertThatThrownBy(() -> registrationService.update(1L, update))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("仅已挂号记录允许修改");
    }

    @Test
    @DisplayName("update succeeds when status is REGISTERED")
    void update_registered_succeeds() {
        var existing = new Registration(1L, 1L, 2L, 3L, 4L, "普通门诊", LocalDateTime.now(), "REGISTERED");
        when(registrationMapper.findById(1L)).thenReturn(existing);

        var update = new Registration(null, 1L, 2L, 3L, 4L, "急诊", null, null);
        var result = registrationService.update(1L, update);

        assertThat(result.visitType()).isEqualTo("急诊");
        verify(registrationMapper).update(any(Registration.class));
    }

    // ── delete ──

    @Test
    @DisplayName("delete only allowed when status is REGISTERED")
    void delete_visitedRegistration_throws() {
        var existing = new Registration(1L, 1L, 2L, 3L, 4L, "普通门诊", LocalDateTime.now(), "VISITED");
        when(registrationMapper.findById(1L)).thenReturn(existing);

        assertThatThrownBy(() -> registrationService.delete(1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("已接诊或已取消的挂号不可取消");
    }

    @Test
    @DisplayName("delete calls cancelById when status is REGISTERED")
    void delete_registered_succeeds() {
        var existing = new Registration(1L, 1L, 2L, 3L, 4L, "普通门诊", LocalDateTime.now(), "REGISTERED");
        when(registrationMapper.findById(1L)).thenReturn(existing);

        registrationService.delete(1L);

        verify(registrationMapper).cancelById(1L);
    }

    // ── findById ──

    @Test
    @DisplayName("findById throws when registration not found")
    void findById_notFound_throws() {
        when(registrationMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> registrationService.findById(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("挂号记录不存在");
    }
}
