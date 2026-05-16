package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.SessionUser;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.AuthService;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.StatisticsService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(AuthService.LOGIN_USER);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.ok(statisticsService.dashboard(null, "ADMIN", null)));
        }
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.dashboard(user.id(), user.role(), null)));
    }

    @GetMapping("/department-schedules")
    public ResponseEntity<ApiResponse<?>> deptSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                statisticsService.departmentScheduleDetail(departmentId, startDate, endDate)));
        }
        return ResponseEntity.ok(ApiResponse.ok(
            statisticsService.departmentScheduleStats(startDate, endDate)));
    }

    @GetMapping("/doctor-workload")
    public ResponseEntity<ApiResponse<?>> doctorWorkload(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok(
            statisticsService.doctorWorkloadStats(doctorId, departmentId, startDate, endDate)));
    }

    @GetMapping("/outpatient-summary")
    public ResponseEntity<ApiResponse<?>> outpatientSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.ok(
            statisticsService.outpatientSummary(startDate, endDate)));
    }

    @GetMapping("/inpatient-summary")
    public ResponseEntity<ApiResponse<?>> inpatientSummary() {
        Map<String, Object> status = statisticsService.inpatientSummary();
        Map<String, Object> records = statisticsService.inpatientRecordSummary(
            LocalDate.now().minusMonths(1), LocalDate.now());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("status", status, "records", records)));
    }

    @GetMapping("/patient-detail")
    public ResponseEntity<ApiResponse<?>> patientDetail(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.ok(
            statisticsService.patientTreatmentDetail(startDate, endDate)));
    }
}
