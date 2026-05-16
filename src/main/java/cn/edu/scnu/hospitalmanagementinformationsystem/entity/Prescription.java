package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Prescription(Long id, Long visitId, Long doctorId, Long patientId,
                           BigDecimal consultationFee, BigDecimal medicineAmount, BigDecimal totalAmount,
                           String status, LocalDateTime createdAt) {}
