package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.dto.AdmissionDto;
import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Admission;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdmissionMapper {
    List<Admission> findAll(@Param("patientId") Long patientId, @Param("doctorId") Long doctorId);

    List<AdmissionDto> findAllWithNames(@Param("patientId") Long patientId, @Param("doctorId") Long doctorId);

    Admission findById(@Param("id") Long id);

    int insert(Admission admission);

    int update(Admission admission);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updatePrepaidBalance(@Param("id") Long id, @Param("balance") BigDecimal balance);

    int discharge(@Param("id") Long id, @Param("dischargedAt") LocalDateTime dischargedAt,
                  @Param("dischargeType") String dischargeType,
                  @Param("prepaidBalance") BigDecimal prepaidBalance);
}
