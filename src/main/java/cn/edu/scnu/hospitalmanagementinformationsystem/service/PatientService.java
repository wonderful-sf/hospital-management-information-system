package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Patient;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.PatientMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PatientService {
    private final PatientMapper patientMapper;

    public PatientService(PatientMapper patientMapper) {
        this.patientMapper = patientMapper;
    }

    public List<Patient> findAll(String keyword) {
        return patientMapper.findAll(keyword);
    }

    public Patient findById(Long id) {
        return patientMapper.findById(id).orElseThrow(() -> new RuntimeException("病人不存在"));
    }

    public Patient create(Patient patient) {
        patientMapper.insert(patient);
        return patient;
    }

    public Patient update(Long id, Patient patient) {
        findById(id);
        Patient updated = new Patient(id, patient.userId(), patient.medicalRecordNo(),
            patient.name(), patient.gender(), patient.phone(), patient.address());
        patientMapper.update(updated);
        return updated;
    }

    public void delete(Long id) {
        findById(id);
        patientMapper.deleteById(id);
    }
}
