package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Medicine;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.MedicineService;
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
@RequestMapping("/api/medicines")
public class MedicineController {
    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Medicine>>> list(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.findAll(keyword)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Medicine>> create(@RequestBody Medicine medicine) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.create(medicine)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Medicine>> update(@PathVariable Long id, @RequestBody Medicine medicine) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.update(id, medicine)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
