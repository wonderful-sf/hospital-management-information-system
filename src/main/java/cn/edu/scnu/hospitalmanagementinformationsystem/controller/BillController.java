package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Bill;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.BillItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Payment;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.BillService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
public class BillController {
    private final BillService billService;
    public BillController(BillService s) { this.billService = s; }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Bill>>> list(@RequestParam(required=false) Long patientId,
            @RequestParam(required=false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(billService.findAll(patientId, status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Bill>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(billService.findById(id)));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<BillItem>>> items(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(billService.findItems(id)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats(@RequestParam(required=false) Long patientId) {
        return ResponseEntity.ok(ApiResponse.ok(billService.stats(patientId)));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<Payment>> pay(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(billService.pay(id)));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<Payment>>> payments(@RequestParam(required=false) Long patientId) {
        return ResponseEntity.ok(ApiResponse.ok(billService.findPayments(patientId)));
    }
}
