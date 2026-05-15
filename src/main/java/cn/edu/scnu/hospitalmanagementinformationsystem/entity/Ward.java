package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;

public record Ward(
    Long id,
    Long departmentId,
    String wardNo,
    String location,
    BigDecimal dailyCharge
) {
}
