package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Bed;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.BedService;
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
@RequestMapping("/api/beds")
public class BedController {
    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Bed>>> list(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(bedService.findAll(keyword)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Bed>> create(@RequestBody Bed bed) {
        return ResponseEntity.ok(ApiResponse.ok(bedService.create(bed)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Bed>> update(@PathVariable Long id, @RequestBody Bed bed) {
        return ResponseEntity.ok(ApiResponse.ok(bedService.update(id, bed)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        bedService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
