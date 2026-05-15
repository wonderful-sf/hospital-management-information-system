package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Doctor;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.DoctorMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DoctorService {
    private final DoctorMapper doctorMapper;

    public DoctorService(DoctorMapper doctorMapper) {
        this.doctorMapper = doctorMapper;
    }

    public List<Doctor> findAll(String keyword) {
        return doctorMapper.findAll(keyword);
    }

    public Doctor findById(Long id) {
        return doctorMapper.findById(id).orElseThrow(() -> new RuntimeException("医生不存在"));
    }

    public Doctor create(Doctor doctor) {
        doctorMapper.insert(doctor);
        return doctor;
    }

    public Doctor update(Long id, Doctor doctor) {
        findById(id);
        Doctor updated = new Doctor(id, doctor.userId(), doctor.departmentId(), doctor.titleId(),
            doctor.employeeNo(), doctor.name(), doctor.gender(), doctor.phone());
        doctorMapper.update(updated);
        return updated;
    }

    public void delete(Long id) {
        findById(id);
        doctorMapper.deleteById(id);
    }
}
