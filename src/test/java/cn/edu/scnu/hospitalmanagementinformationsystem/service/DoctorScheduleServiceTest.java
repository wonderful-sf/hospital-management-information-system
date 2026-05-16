package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.DoctorSchedule;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.DoctorScheduleMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceTest {

    @Mock
    private DoctorScheduleMapper doctorScheduleMapper;

    private DoctorScheduleService doctorScheduleService;

    private final LocalDateTime futureStart = LocalDateTime.of(2026, 6, 1, 8, 0);
    private final LocalDateTime futureEnd = LocalDateTime.of(2026, 6, 1, 12, 0);
    private final LocalDateTime pastStart = LocalDateTime.of(2026, 1, 1, 8, 0);
    private final LocalDateTime pastEnd = LocalDateTime.of(2026, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        doctorScheduleService = new DoctorScheduleService(doctorScheduleMapper);
    }

    // ── create ──

    @Test
    @DisplayName("create throws when doctorId is missing")
    void create_missingDoctorId_throws() {
        var schedule = new DoctorSchedule(null, null, 1L, "门诊", futureStart, futureEnd, "A101");

        assertThatThrownBy(() -> doctorScheduleService.create(schedule))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("排班必须关联医生和科室");
    }

    @Test
    @DisplayName("create throws when start time is after end time")
    void create_endBeforeStart_throws() {
        var schedule = new DoctorSchedule(null, 1L, 1L, "门诊", futureEnd, futureStart, "A101");

        assertThatThrownBy(() -> doctorScheduleService.create(schedule))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("排班结束时间必须晚于开始时间");
    }

    @Test
    @DisplayName("create throws when conflicting schedule exists")
    void create_conflict_throws() {
        var schedule = new DoctorSchedule(null, 1L, 1L, "门诊", futureStart, futureEnd, "A101");
        when(doctorScheduleMapper.countConflicts(1L, futureStart, futureEnd, null)).thenReturn(1);

        assertThatThrownBy(() -> doctorScheduleService.create(schedule))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("同一医生排班时间冲突");
    }

    @Test
    @DisplayName("create succeeds for valid schedule")
    void create_valid_succeeds() {
        var schedule = new DoctorSchedule(null, 1L, 1L, "门诊", futureStart, futureEnd, "A101");
        when(doctorScheduleMapper.countConflicts(1L, futureStart, futureEnd, null)).thenReturn(0);
        when(doctorScheduleMapper.lastInsertId()).thenReturn(10L);

        var result = doctorScheduleService.create(schedule);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.doctorId()).isEqualTo(1L);
        assertThat(result.scheduleType()).isEqualTo("门诊");
        assertThat(result.room()).isEqualTo("A101");
        verify(doctorScheduleMapper).insert(schedule);
    }

    // ── update ──

    @Test
    @DisplayName("update throws when schedule is historical")
    void update_historical_throws() {
        var existing = new DoctorSchedule(1L, 1L, 1L, "门诊", pastStart, pastEnd, "A101");
        when(doctorScheduleMapper.findById(1L)).thenReturn(existing);

        var update = new DoctorSchedule(null, 1L, 1L, "门诊", futureStart, futureEnd, "B202");

        assertThatThrownBy(() -> doctorScheduleService.update(1L, update))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("历史排班不可修改");
    }

    @Test
    @DisplayName("update throws when new schedule conflicts")
    void update_conflict_throws() {
        var existing = new DoctorSchedule(1L, 1L, 1L, "门诊", futureStart, futureEnd, "A101");
        when(doctorScheduleMapper.findById(1L)).thenReturn(existing);
        when(doctorScheduleMapper.countConflicts(1L, futureStart, futureEnd, 1L)).thenReturn(1);

        var update = new DoctorSchedule(null, 1L, 1L, "门诊", futureStart, futureEnd, "B202");

        assertThatThrownBy(() -> doctorScheduleService.update(1L, update))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("同一医生排班时间冲突");
    }

    @Test
    @DisplayName("update succeeds for future schedule with no conflict")
    void update_valid_succeeds() {
        var existing = new DoctorSchedule(1L, 1L, 1L, "门诊", futureStart, futureEnd, "A101");
        when(doctorScheduleMapper.findById(1L)).thenReturn(existing);
        when(doctorScheduleMapper.countConflicts(1L, futureStart, futureEnd, 1L)).thenReturn(0);

        var update = new DoctorSchedule(null, 1L, 1L, "门诊", futureStart, futureEnd, "B202");
        var result = doctorScheduleService.update(1L, update);

        assertThat(result.room()).isEqualTo("B202");
        verify(doctorScheduleMapper).update(any(DoctorSchedule.class));
    }

    // ── delete ──

    @Test
    @DisplayName("delete throws when schedule is historical")
    void delete_historical_throws() {
        var schedule = new DoctorSchedule(1L, 1L, 1L, "门诊", pastStart, pastEnd, "A101");
        when(doctorScheduleMapper.findById(1L)).thenReturn(schedule);

        assertThatThrownBy(() -> doctorScheduleService.delete(1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("历史排班不可删除");
    }

    @Test
    @DisplayName("delete succeeds for future schedule")
    void delete_future_succeeds() {
        var schedule = new DoctorSchedule(1L, 1L, 1L, "门诊", futureStart, futureEnd, "A101");
        when(doctorScheduleMapper.findById(1L)).thenReturn(schedule);

        doctorScheduleService.delete(1L);

        verify(doctorScheduleMapper).deleteById(1L);
    }

    // ── findById ──

    @Test
    @DisplayName("findById throws when schedule not found")
    void findById_notFound_throws() {
        when(doctorScheduleMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> doctorScheduleService.findById(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("排班不存在");
    }
}
