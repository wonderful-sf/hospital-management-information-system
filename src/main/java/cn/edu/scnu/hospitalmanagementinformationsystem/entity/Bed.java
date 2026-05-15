package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

public record Bed(
    Long id,
    Long wardId,
    String bedNo,
    BedStatus status
) {
}
