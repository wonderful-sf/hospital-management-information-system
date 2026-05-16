package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.OutpatientVisit;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Registration;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.OutpatientVisitMapper;
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
class OutpatientVisitServiceTest {

    @Mock
    private OutpatientVisitMapper outpatientVisitMapper;
    @Mock
    private RegistrationMapper registrationMapper;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private OutpatientVisitService outpatientVisitService;

    @BeforeEach
    void setUp() {
        outpatientVisitService = new OutpatientVisitService(outpatientVisitMapper, registrationMapper, jdbcTemplate);
    }

    // ── create ──

    @Test
    @DisplayName("create throws when registration does not exist")
    void create_registrationNotFound_throws() {
        var visit = new OutpatientVisit(null, 999L, null, null, "headache", "cold", null);
        when(registrationMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> outpatientVisitService.create(visit))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("挂号记录不存在");
    }

    @Test
    @DisplayName("create throws when registration is not REGISTERED")
    void create_registrationNotRegistered_throws() {
        var registration = new Registration(1L, 100L, 200L, 1L, 1L, "普通门诊", LocalDateTime.now(), "VISITED");
        var visit = new OutpatientVisit(null, 1L, null, null, "headache", "cold", null);
        when(registrationMapper.findById(1L)).thenReturn(registration);

        assertThatThrownBy(() -> outpatientVisitService.create(visit))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("仅已挂号记录允许接诊");
    }

    @Test
    @DisplayName("create throws when registration already has a visit")
    void create_duplicateVisit_throws() {
        var registration = new Registration(1L, 100L, 200L, 1L, 1L, "普通门诊", LocalDateTime.now(), "REGISTERED");
        var visit = new OutpatientVisit(null, 1L, null, null, "headache", "cold", null);
        when(registrationMapper.findById(1L)).thenReturn(registration);
        when(outpatientVisitMapper.findByRegistrationId(1L)).thenReturn(new OutpatientVisit(5L, 1L, 100L, 200L, "fever", "flu", LocalDateTime.now()));

        assertThatThrownBy(() -> outpatientVisitService.create(visit))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("该挂号记录已接诊");
    }

    @Test
    @DisplayName("create succeeds and updates registration status to VISITED")
    void create_valid_succeeds() {
        var registration = new Registration(1L, 100L, 200L, 1L, 1L, "普通门诊", LocalDateTime.now(), "REGISTERED");
        var visit = new OutpatientVisit(null, 1L, null, null, "headache", "cold", null);
        when(registrationMapper.findById(1L)).thenReturn(registration);
        when(outpatientVisitMapper.findByRegistrationId(1L)).thenReturn(null);
        when(jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class)).thenReturn(50L);

        var result = outpatientVisitService.create(visit);

        assertThat(result.id()).isEqualTo(50L);
        assertThat(result.registrationId()).isEqualTo(1L);
        assertThat(result.patientId()).isEqualTo(100L);
        assertThat(result.doctorId()).isEqualTo(200L);
        assertThat(result.symptomDescription()).isEqualTo("headache");
        assertThat(result.diagnosis()).isEqualTo("cold");

        verify(outpatientVisitMapper).insert(any(OutpatientVisit.class));
        verify(registrationMapper).updateStatus(1L, "VISITED");
    }

    // ── findById ──

    @Test
    @DisplayName("findById throws when visit not found")
    void findById_notFound_throws() {
        when(outpatientVisitMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> outpatientVisitService.findById(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("接诊记录不存在");
    }
}
