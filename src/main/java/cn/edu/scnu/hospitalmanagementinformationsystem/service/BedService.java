package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Bed;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.BedStatus;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.BedMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BedService {
    private final BedMapper bedMapper;

    public BedService(BedMapper bedMapper) {
        this.bedMapper = bedMapper;
    }

    public List<Bed> findAll(String keyword) {
        return bedMapper.findAll(keyword);
    }

    public Bed findById(Long id) {
        return bedMapper.findById(id).orElseThrow(() -> new RuntimeException("病床不存在"));
    }

    public Bed create(Bed bed) {
        bedMapper.insert(bed);
        return bed;
    }

    public Bed update(Long id, Bed bed) {
        findById(id);
        Bed updated = new Bed(id, bed.wardId(), bed.bedNo(), bed.status());
        bedMapper.update(updated);
        return updated;
    }

    public void delete(Long id) {
        findById(id);
        bedMapper.updateStatus(id, BedStatus.MAINTENANCE.name());
    }
}
