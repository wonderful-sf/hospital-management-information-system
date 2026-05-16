package cn.edu.scnu.hospitalmanagementinformationsystem.controller;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.ApiResponse;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Registration;
import cn.edu.scnu.hospitalmanagementinformationsystem.service.RegistrationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Registration>>> list(@RequestParam(required = false) Long patientId) {
        return ResponseEntity.ok(ApiResponse.ok(registrationService.findAll(patientId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Registration>> create(@RequestBody Registration registration) {
        return ResponseEntity.ok(ApiResponse.ok(registrationService.create(registration)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Registration>> update(@PathVariable Long id, @RequestBody Registration registration) {
        return ResponseEntity.ok(ApiResponse.ok(registrationService.update(id, registration)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        registrationService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
