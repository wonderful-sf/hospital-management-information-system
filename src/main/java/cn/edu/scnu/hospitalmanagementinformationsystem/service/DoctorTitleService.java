package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.DoctorTitle;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.DoctorTitleMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorTitleService {
    private final DoctorTitleMapper doctorTitleMapper;

    public DoctorTitleService(DoctorTitleMapper doctorTitleMapper) {
        this.doctorTitleMapper = doctorTitleMapper;
    }

    public List<DoctorTitle> findAll(String keyword) {
        return doctorTitleMapper.findAll(keyword);
    }

    public DoctorTitle findById(Long id) {
        DoctorTitle title = doctorTitleMapper.findById(id);
        if (title == null) {
            throw new RuntimeException("职称不存在");
        }
        return title;
    }

    @Transactional
    public DoctorTitle create(DoctorTitle doctorTitle) {
        doctorTitleMapper.insert(doctorTitle);
        Long id = doctorTitleMapper.lastInsertId();
        return new DoctorTitle(id, doctorTitle.name(), doctorTitle.consultationFee());
    }

    public DoctorTitle update(Long id, DoctorTitle doctorTitle) {
        findById(id);
        DoctorTitle updated = new DoctorTitle(id, doctorTitle.name(), doctorTitle.consultationFee());
        doctorTitleMapper.update(updated);
        return updated;
    }

    public void delete(Long id) {
        findById(id);
        doctorTitleMapper.deleteById(id);
    }
}
