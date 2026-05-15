package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Patient;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PatientMapper {
    List<Patient> findAll(@Param("keyword") String keyword);

    Optional<Patient> findById(@Param("id") Long id);

    int insert(Patient patient);

    int update(Patient patient);

    int deleteById(@Param("id") Long id);
}
