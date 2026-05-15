package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;

public record Medicine(
    Long id,
    String code,
    String name,
    String specification,
    String unit,
    BigDecimal unitPrice,
    Integer stockQuantity,
    MedicineStatus status
) {
}
