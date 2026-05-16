package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.PrescriptionCreateRequest;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.PrescriptionUpdateRequest;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Prescription;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrescriptionItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.PrescriptionItemService;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.PrescriptionService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final PrescriptionItemService prescriptionItemService;

    public PrescriptionController(PrescriptionService prescriptionService, PrescriptionItemService prescriptionItemService) {
        this.prescriptionService = prescriptionService;
        this.prescriptionItemService = prescriptionItemService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Prescription>>> list(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        return ResponseEntity.ok(ApiResponse.ok(prescriptionService.findAll(patientId, doctorId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Prescription>> create(@RequestBody PrescriptionCreateRequest request) {
        Prescription prescription = new Prescription(null, request.visitId(), request.doctorId(),
            request.patientId(), request.consultationFee(), null, null, null, null);
        return ResponseEntity.ok(ApiResponse.ok(prescriptionService.create(prescription, request.items())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Prescription>> update(@PathVariable Long id, @RequestBody PrescriptionUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(prescriptionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        prescriptionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<PrescriptionItem>>> items(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(prescriptionItemService.findByPrescriptionId(id)));
    }
}
