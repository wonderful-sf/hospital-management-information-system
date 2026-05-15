package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

public record Patient(
    Long id,
    Long userId,
    String medicalRecordNo,
    String name,
    Gender gender,
    String phone,
    String address
) {
}
