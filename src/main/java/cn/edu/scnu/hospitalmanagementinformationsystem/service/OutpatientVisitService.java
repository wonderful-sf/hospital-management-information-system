package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.OutpatientVisit;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Registration;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.OutpatientVisitMapper;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.RegistrationMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutpatientVisitService {
    private final OutpatientVisitMapper outpatientVisitMapper;
    private final RegistrationMapper registrationMapper;
    private final JdbcTemplate jdbcTemplate;

    public OutpatientVisitService(OutpatientVisitMapper outpatientVisitMapper,
                                  RegistrationMapper registrationMapper,
                                  JdbcTemplate jdbcTemplate) {
        this.outpatientVisitMapper = outpatientVisitMapper;
        this.registrationMapper = registrationMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OutpatientVisit> findAll(Long doctorId, Long patientId) {
        return outpatientVisitMapper.findAll(doctorId, patientId);
    }

    public OutpatientVisit findById(Long id) {
        OutpatientVisit visit = outpatientVisitMapper.findById(id);
        if (visit == null) {
            throw new RuntimeException("接诊记录不存在");
        }
        return visit;
    }

    @Transactional
    public OutpatientVisit create(OutpatientVisit visit) {
        Registration registration = registrationMapper.findById(visit.registrationId());
        if (registration == null) {
            throw new RuntimeException("挂号记录不存在");
        }
        if (!"REGISTERED".equals(registration.status())) {
            throw new RuntimeException("仅已挂号记录允许接诊");
        }
        if (outpatientVisitMapper.findByRegistrationId(visit.registrationId()) != null) {
            throw new RuntimeException("该挂号记录已接诊");
        }
        OutpatientVisit toSave = new OutpatientVisit(null, registration.id(), registration.patientId(),
            registration.doctorId(), visit.symptomDescription(), visit.diagnosis(), LocalDateTime.now());
        outpatientVisitMapper.insert(toSave);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        registrationMapper.updateStatus(registration.id(), "VISITED");
        return new OutpatientVisit(id, toSave.registrationId(), toSave.patientId(), toSave.doctorId(),
            toSave.symptomDescription(), toSave.diagnosis(), toSave.visitedAt());
    }

    public OutpatientVisit update(Long id, OutpatientVisit visit) {
        OutpatientVisit existing = findById(id);
        OutpatientVisit updated = new OutpatientVisit(id, existing.registrationId(), existing.patientId(),
            existing.doctorId(), visit.symptomDescription(), visit.diagnosis(), existing.visitedAt());
        outpatientVisitMapper.update(updated);
        return updated;
    }
}
