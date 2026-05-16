package cn.edu.scnu.hospitalmanagementinformationsystem.entity;

import java.time.LocalDateTime;

public record OutpatientVisit(Long id, Long registrationId, Long patientId, Long doctorId,
                              String symptomDescription, String diagnosis, LocalDateTime visitedAt) {}
