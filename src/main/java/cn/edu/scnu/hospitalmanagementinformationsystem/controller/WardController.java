package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Ward;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.WardService;
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
@RequestMapping("/api/wards")
public class WardController {
    private final WardService wardService;

    public WardController(WardService wardService) {
        this.wardService = wardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Ward>>> list(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(wardService.findAll(keyword)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Ward>> create(@RequestBody Ward ward) {
        return ResponseEntity.ok(ApiResponse.ok(wardService.create(ward)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Ward>> update(@PathVariable Long id, @RequestBody Ward ward) {
        return ResponseEntity.ok(ApiResponse.ok(wardService.update(id, ward)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        wardService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
