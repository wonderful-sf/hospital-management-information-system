package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.OutpatientVisit;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.OutpatientVisitService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visits")
public class OutpatientVisitController {
    private final OutpatientVisitService outpatientVisitService;

    public OutpatientVisitController(OutpatientVisitService outpatientVisitService) {
        this.outpatientVisitService = outpatientVisitService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OutpatientVisit>>> list(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId) {
        return ResponseEntity.ok(ApiResponse.ok(outpatientVisitService.findAll(doctorId, patientId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OutpatientVisit>> create(@RequestBody OutpatientVisit visit) {
        return ResponseEntity.ok(ApiResponse.ok(outpatientVisitService.create(visit)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OutpatientVisit>> update(@PathVariable Long id, @RequestBody OutpatientVisit visit) {
        return ResponseEntity.ok(ApiResponse.ok(outpatientVisitService.update(id, visit)));
    }
}
