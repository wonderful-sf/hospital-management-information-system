package cn.edu.scnu.hospitalmanagementinformationsystem.dto;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.InpatientRecordItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InpatientRecordCreateRequest(
    Long admissionId,
    LocalDate recordDate,
    String conditionDescription,
    String treatmentSummary,
    BigDecimal treatmentFee,
    List<InpatientRecordItem> items
) {}
