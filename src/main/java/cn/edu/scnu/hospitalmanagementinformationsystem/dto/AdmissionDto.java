package cn.edu.scnu.hospitalmanagementinformationsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdmissionDto(
    Long id, String admissionNo, Long patientId, Long departmentId, Long attendingDoctorId,
    Long bedId, LocalDateTime admittedAt, LocalDateTime dischargedAt, String dischargeType,
    BigDecimal prepaidBalance, String status,
    String patientName, String departmentName, String doctorName, String bedNo
) {}
