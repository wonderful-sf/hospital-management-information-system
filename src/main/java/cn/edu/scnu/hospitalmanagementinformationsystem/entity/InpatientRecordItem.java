package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;

public record InpatientRecordItem(
    Long id,
    Long inpatientRecordId,
    InpatientRecordItemType itemType,
    Long medicineId,
    String itemName,
    BigDecimal unitPrice,
    BigDecimal quantity,
    String usageInstruction,
    BigDecimal amount
) {}
