package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.time.LocalDateTime;

public record DoctorSchedule(Long id, Long doctorId, Long departmentId, String scheduleType,
                             LocalDateTime startTime, LocalDateTime endTime, String room) {}
