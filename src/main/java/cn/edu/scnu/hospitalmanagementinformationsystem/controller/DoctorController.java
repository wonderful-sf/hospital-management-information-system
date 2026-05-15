package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Doctor;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.DoctorService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> list(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(doctorService.findAll(keyword)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Doctor>> create(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(ApiResponse.ok(doctorService.create(doctor)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Doctor>> update(@PathVariable Long id, @RequestBody Doctor doctor) {
        return ResponseEntity.ok(ApiResponse.ok(doctorService.update(id, doctor)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
