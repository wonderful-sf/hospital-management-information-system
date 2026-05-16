package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.OutpatientVisit;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OutpatientVisitMapper {
    List<OutpatientVisit> findAll(@Param("doctorId") Long doctorId, @Param("patientId") Long patientId);

    OutpatientVisit findById(@Param("id") Long id);

    OutpatientVisit findByRegistrationId(@Param("registrationId") Long registrationId);

    int insert(OutpatientVisit outpatientVisit);

    int update(OutpatientVisit outpatientVisit);
}
