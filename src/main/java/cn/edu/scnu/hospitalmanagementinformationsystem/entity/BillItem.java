package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;

public record BillItem(Long id, Long billId, String itemType, String itemName,
    BigDecimal unitPrice, BigDecimal quantity, BigDecimal amount) {}
