package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrescriptionItem;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.PrescriptionItemMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionItemService {
    private final PrescriptionItemMapper prescriptionItemMapper;

    public PrescriptionItemService(PrescriptionItemMapper prescriptionItemMapper) {
        this.prescriptionItemMapper = prescriptionItemMapper;
    }

    public List<PrescriptionItem> findByPrescriptionId(Long prescriptionId) {
        return prescriptionItemMapper.findByPrescriptionId(prescriptionId);
    }
}
