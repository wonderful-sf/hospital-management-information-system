package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Registration;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.RegistrationMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private final RegistrationMapper registrationMapper;
    private final JdbcTemplate jdbcTemplate;

    public RegistrationService(RegistrationMapper registrationMapper, JdbcTemplate jdbcTemplate) {
        this.registrationMapper = registrationMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Registration> findAll(Long patientId) {
        return registrationMapper.findAll(patientId);
    }

    public Registration findById(Long id) {
        Registration registration = registrationMapper.findById(id);
        if (registration == null) {
            throw new RuntimeException("挂号记录不存在");
        }
        return registration;
    }

    @Transactional
    public Registration create(Registration registration) {
        if (registration.patientId() == null || registration.doctorId() == null || registration.departmentId() == null) {
            throw new RuntimeException("挂号必须关联病人、医生和科室");
        }
        if (registration.visitType() == null || registration.visitType().isBlank()) {
            throw new RuntimeException("就诊类型不能为空");
        }
        Registration toSave = new Registration(null, registration.patientId(), registration.doctorId(),
            registration.departmentId(), registration.scheduleId(), registration.visitType(),
            LocalDateTime.now(), "REGISTERED");
        registrationMapper.insert(toSave);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return new Registration(id, toSave.patientId(), toSave.doctorId(), toSave.departmentId(),
            toSave.scheduleId(), toSave.visitType(), toSave.registeredAt(), toSave.status());
    }

    public Registration update(Long id, Registration registration) {
        Registration existing = findById(id);
        if (!"REGISTERED".equals(existing.status())) {
            throw new RuntimeException("仅已挂号记录允许修改");
        }
        Registration updated = new Registration(id, registration.patientId(), registration.doctorId(),
            registration.departmentId(), registration.scheduleId(), registration.visitType(),
            existing.registeredAt(), existing.status());
        registrationMapper.update(updated);
        return updated;
    }

    public void delete(Long id) {
        Registration existing = findById(id);
        if (!"REGISTERED".equals(existing.status())) {
            throw new RuntimeException("已接诊或已取消的挂号不可取消");
        }
        registrationMapper.cancelById(id);
    }
}
