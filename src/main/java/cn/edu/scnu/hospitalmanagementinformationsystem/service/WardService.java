package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Ward;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.WardMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WardService {
    private final WardMapper wardMapper;

    public WardService(WardMapper wardMapper) {
        this.wardMapper = wardMapper;
    }

    public List<Ward> findAll(String keyword) {
        return wardMapper.findAll(keyword);
    }

    public Ward findById(Long id) {
        return wardMapper.findById(id).orElseThrow(() -> new RuntimeException("病房不存在"));
    }

    public Ward create(Ward ward) {
        wardMapper.insert(ward);
        return ward;
    }

    public Ward update(Long id, Ward ward) {
        findById(id);
        Ward updated = new Ward(id, ward.departmentId(), ward.wardNo(),
            ward.location(), ward.dailyCharge());
        wardMapper.update(updated);
        return updated;
    }

    public void delete(Long id) {
        findById(id);
        wardMapper.deleteById(id);
    }
}
