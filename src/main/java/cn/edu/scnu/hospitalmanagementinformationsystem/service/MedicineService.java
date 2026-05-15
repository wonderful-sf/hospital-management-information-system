package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Medicine;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.MedicineMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MedicineService {
    private final MedicineMapper medicineMapper;

    public MedicineService(MedicineMapper medicineMapper) {
        this.medicineMapper = medicineMapper;
    }

    public List<Medicine> findAll(String keyword) {
        return medicineMapper.findAll(keyword);
    }

    public Medicine findById(Long id) {
        return medicineMapper.findById(id).orElseThrow(() -> new RuntimeException("药品不存在"));
    }

    public Medicine create(Medicine medicine) {
        medicineMapper.insert(medicine);
        return medicine;
    }

    public Medicine update(Long id, Medicine medicine) {
        findById(id);
        Medicine updated = new Medicine(id, medicine.code(), medicine.name(),
            medicine.specification(), medicine.unit(), medicine.unitPrice(),
            medicine.stockQuantity(), medicine.status());
        medicineMapper.update(updated);
        return updated;
    }

    public void delete(Long id) {
        findById(id);
        medicineMapper.disableById(id);
    }
}
