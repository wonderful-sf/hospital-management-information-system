package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.AdmissionCreateRequest;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.AdmissionDto;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.PrepaidDepositRequest;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Admission;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrepaidRecord;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.AdmissionService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admissions")
public class AdmissionController {
    private final AdmissionService admissionService;

    public AdmissionController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdmissionDto>>> list(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        return ResponseEntity.ok(ApiResponse.ok(admissionService.findAll(patientId, doctorId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Admission>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(admissionService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Admission>> create(@RequestBody AdmissionCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
            admissionService.create(request.patientId(), request.departmentId(),
                request.attendingDoctorId(), request.bedId(), request.initialDeposit())));
    }

    @PostMapping("/{id}/prepaid")
    public ResponseEntity<ApiResponse<PrepaidRecord>> deposit(@PathVariable Long id,
                                                               @RequestBody PrepaidDepositRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(admissionService.deposit(id, request.amount())));
    }

    @PostMapping("/{id}/discharge")
    public ResponseEntity<ApiResponse<Void>> discharge(@PathVariable Long id,
                                                        @RequestParam(defaultValue = "NORMAL") String dischargeType) {
        admissionService.discharge(id, dischargeType);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}/prepaid-records")
    public ResponseEntity<ApiResponse<List<PrepaidRecord>>> prepaidRecords(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(admissionService.findPrepaidRecords(id)));
    }
}
