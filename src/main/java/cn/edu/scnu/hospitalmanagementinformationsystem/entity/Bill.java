package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Bill(Long id, String billNo, Long patientId, String sourceType, Long sourceId,
    BigDecimal totalAmount, String status, LocalDateTime paidAt, LocalDateTime createdAt) {}
