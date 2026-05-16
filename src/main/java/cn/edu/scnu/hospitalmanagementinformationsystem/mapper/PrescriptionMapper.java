package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Prescription;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PrescriptionMapper {
    List<Prescription> findAll(@Param("patientId") Long patientId, @Param("doctorId") Long doctorId);

    Prescription findById(@Param("id") Long id);

    Prescription findByVisitId(@Param("visitId") Long visitId);

    int insert(Prescription prescription);

    int update(Prescription prescription);

    int cancelById(@Param("id") Long id);
}
