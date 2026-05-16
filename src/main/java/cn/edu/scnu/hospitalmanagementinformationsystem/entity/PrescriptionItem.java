package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;

public record PrescriptionItem(Long id, Long prescriptionId, Long medicineId, String medicineName,
                               BigDecimal unitPrice, Integer quantity, String usageInstruction,
                               BigDecimal amount) {}
