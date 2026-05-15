package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

public record Doctor(
    Long id,
    Long userId,
    Long departmentId,
    Long titleId,
    String employeeNo,
    String name,
    Gender gender,
    String phone
) {
}
