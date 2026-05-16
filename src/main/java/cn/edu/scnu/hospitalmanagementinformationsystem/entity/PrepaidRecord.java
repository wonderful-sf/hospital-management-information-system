package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrepaidRecord(
    Long id,
    String recordNo,
    Long admissionId,
    Long patientId,
    BigDecimal amount,
    String type,
    BigDecimal balanceAfter,
    String remark,
    LocalDateTime createdAt
) {}
