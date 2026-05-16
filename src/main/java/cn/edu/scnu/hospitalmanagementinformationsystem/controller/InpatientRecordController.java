package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.dto.InpatientRecordCreateRequest;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.InpatientRecord;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.InpatientRecordItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.InpatientRecordService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inpatient-records")
public class InpatientRecordController {
    private final InpatientRecordService inpatientRecordService;

    public InpatientRecordController(InpatientRecordService inpatientRecordService) {
        this.inpatientRecordService = inpatientRecordService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InpatientRecord>>> list(@RequestParam Long admissionId) {
        return ResponseEntity.ok(ApiResponse.ok(inpatientRecordService.findByAdmissionId(admissionId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InpatientRecord>> create(@RequestBody InpatientRecordCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(inpatientRecordService.create(request)));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<InpatientRecordItem>>> items(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inpatientRecordService.findItemsByRecordId(id)));
    }
}
