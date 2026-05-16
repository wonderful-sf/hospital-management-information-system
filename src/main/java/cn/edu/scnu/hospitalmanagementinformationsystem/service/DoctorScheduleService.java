package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.DoctorSchedule;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.DoctorScheduleMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorScheduleService {
    private final DoctorScheduleMapper doctorScheduleMapper;

    public DoctorScheduleService(DoctorScheduleMapper doctorScheduleMapper) {
        this.doctorScheduleMapper = doctorScheduleMapper;
    }

    public List<DoctorSchedule> findAll(Long doctorId, Long departmentId) {
        return doctorScheduleMapper.findAll(doctorId, departmentId);
    }

    public DoctorSchedule findById(Long id) {
        DoctorSchedule schedule = doctorScheduleMapper.findById(id);
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }
        return schedule;
    }

    @Transactional
    public DoctorSchedule create(DoctorSchedule schedule) {
        validateSchedule(schedule, null);
        doctorScheduleMapper.insert(schedule);
        Long id = doctorScheduleMapper.lastInsertId();
        return new DoctorSchedule(id, schedule.doctorId(), schedule.departmentId(), schedule.scheduleType(),
            schedule.startTime(), schedule.endTime(), schedule.room());
    }

    public DoctorSchedule update(Long id, DoctorSchedule schedule) {
        DoctorSchedule existing = findById(id);
        if (existing.startTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("历史排班不可修改");
        }
        validateSchedule(schedule, id);
        DoctorSchedule updated = new DoctorSchedule(id, schedule.doctorId(), schedule.departmentId(),
            schedule.scheduleType(), schedule.startTime(), schedule.endTime(), schedule.room());
        doctorScheduleMapper.update(updated);
        return updated;
    }

    public void delete(Long id) {
        DoctorSchedule schedule = findById(id);
        if (schedule.startTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("历史排班不可删除");
        }
        doctorScheduleMapper.deleteById(id);
    }

    private void validateSchedule(DoctorSchedule schedule, Long excludeId) {
        if (schedule.doctorId() == null || schedule.departmentId() == null) {
            throw new RuntimeException("排班必须关联医生和科室");
        }
        if (schedule.startTime() == null || schedule.endTime() == null) {
            throw new RuntimeException("排班开始和结束时间不能为空");
        }
        if (!schedule.endTime().isAfter(schedule.startTime())) {
            throw new RuntimeException("排班结束时间必须晚于开始时间");
        }
        if (doctorScheduleMapper.countConflicts(
            schedule.doctorId(), schedule.startTime(), schedule.endTime(), excludeId) > 0) {
            throw new RuntimeException("同一医生排班时间冲突");
        }
    }
}
