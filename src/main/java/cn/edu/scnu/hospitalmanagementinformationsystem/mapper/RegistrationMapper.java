package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Registration;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RegistrationMapper {
    List<Registration> findAll(@Param("patientId") Long patientId);

    Registration findById(@Param("id") Long id);

    int insert(Registration registration);

    int update(Registration registration);

    int cancelById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
