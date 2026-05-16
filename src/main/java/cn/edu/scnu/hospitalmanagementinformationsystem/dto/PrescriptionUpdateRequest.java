package cn.edu.scnu.hospitalmanagementinformationsystem.dto;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrescriptionItem;
import java.math.BigDecimal;
import java.util.List;

public record PrescriptionUpdateRequest(BigDecimal consultationFee, List<PrescriptionItem> items) {}
