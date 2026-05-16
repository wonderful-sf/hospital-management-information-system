package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.DoctorTitle;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.DoctorTitleService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor-titles")
public class DoctorTitleController {
    private final DoctorTitleService doctorTitleService;

    public DoctorTitleController(DoctorTitleService doctorTitleService) {
        this.doctorTitleService = doctorTitleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorTitle>>> list(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(doctorTitleService.findAll(keyword)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorTitle>> create(@RequestBody DoctorTitle doctorTitle) {
        return ResponseEntity.ok(ApiResponse.ok(doctorTitleService.create(doctorTitle)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorTitle>> update(@PathVariable Long id, @RequestBody DoctorTitle doctorTitle) {
        return ResponseEntity.ok(ApiResponse.ok(doctorTitleService.update(id, doctorTitle)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        doctorTitleService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
