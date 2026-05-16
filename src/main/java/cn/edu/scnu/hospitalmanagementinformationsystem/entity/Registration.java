package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.time.LocalDateTime;

public record Registration(Long id, Long patientId, Long doctorId, Long departmentId, Long scheduleId,
                           String visitType, LocalDateTime registeredAt, String status) {}
