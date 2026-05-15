package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Doctor;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DoctorMapper {
    List<Doctor> findAll(@Param("keyword") String keyword);

    Optional<Doctor> findById(@Param("id") Long id);

    int insert(Doctor doctor);

    int update(Doctor doctor);

    int deleteById(@Param("id") Long id);
}
