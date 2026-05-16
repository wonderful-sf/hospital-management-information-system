package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrescriptionItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PrescriptionItemMapper {
    List<PrescriptionItem> findByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
    int insert(PrescriptionItem item);
    int deleteByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
