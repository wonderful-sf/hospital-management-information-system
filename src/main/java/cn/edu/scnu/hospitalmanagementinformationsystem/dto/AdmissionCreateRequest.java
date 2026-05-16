package cn.edu.scnu.hospitalmanagementinformationsystem.dto;

import java.math.BigDecimal;

public record AdmissionCreateRequest(
    Long patientId,
    Long departmentId,
    Long attendingDoctorId,
    Long bedId,
    BigDecimal initialDeposit
) {}
