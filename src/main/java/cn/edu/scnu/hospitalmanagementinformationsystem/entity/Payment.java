package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(Long id, String paymentNo, Long billId, Long patientId,
    BigDecimal amount, String paymentMethod, LocalDateTime paidAt) {}
