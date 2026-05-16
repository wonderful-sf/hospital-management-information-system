package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Admission(
    Long id,
    String admissionNo,
    Long patientId,
    Long departmentId,
    Long attendingDoctorId,
    Long bedId,
    LocalDateTime admittedAt,
    LocalDateTime dischargedAt,
    String dischargeType,
    BigDecimal prepaidBalance,
    String status
) {}
