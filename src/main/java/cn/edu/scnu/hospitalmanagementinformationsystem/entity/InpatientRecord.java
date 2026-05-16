package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InpatientRecord(
    Long id,
    Long admissionId,
    LocalDate recordDate,
    String conditionDescription,
    String treatmentSummary,
    BigDecimal treatmentFee,
    LocalDateTime createdAt
) {}
