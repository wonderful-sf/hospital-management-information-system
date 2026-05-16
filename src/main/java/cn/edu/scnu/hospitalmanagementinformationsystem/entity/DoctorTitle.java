package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.math.BigDecimal;

public record DoctorTitle(Long id, String name, BigDecimal consultationFee) {}
