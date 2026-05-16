package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.DoctorSchedule;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.DoctorScheduleService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules")
public class DoctorScheduleController {
    private final DoctorScheduleService doctorScheduleService;

    public DoctorScheduleController(DoctorScheduleService doctorScheduleService) {
        this.doctorScheduleService = doctorScheduleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorSchedule>>> list(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok(doctorScheduleService.findAll(doctorId, departmentId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorSchedule>> create(@RequestBody DoctorSchedule schedule) {
        return ResponseEntity.ok(ApiResponse.ok(doctorScheduleService.create(schedule)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorSchedule>> update(@PathVariable Long id, @RequestBody DoctorSchedule schedule) {
        return ResponseEntity.ok(ApiResponse.ok(doctorScheduleService.update(id, schedule)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        doctorScheduleService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
