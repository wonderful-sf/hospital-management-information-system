package cn.edu.scnu.hospitalmanagementinformationsystem.dto;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrescriptionItem;
import java.math.BigDecimal;
import java.util.List;

public record PrescriptionCreateRequest(Long visitId, Long doctorId, Long patientId,
                                        BigDecimal consultationFee, List<PrescriptionItem> items) {}
