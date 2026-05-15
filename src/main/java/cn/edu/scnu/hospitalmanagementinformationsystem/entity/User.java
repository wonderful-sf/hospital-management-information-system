package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

public record User(
    Long id,
    String username,
    String password,
    UserRole role,
    String permissions,
    UserStatus status
) {
}
