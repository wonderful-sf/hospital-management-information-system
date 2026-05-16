package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.DoctorTitle;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DoctorTitleMapper {
    List<DoctorTitle> findAll(@Param("keyword") String keyword);

    DoctorTitle findById(@Param("id") Long id);

    int insert(DoctorTitle doctorTitle);

    int update(DoctorTitle doctorTitle);

    int deleteById(@Param("id") Long id);

    Long lastInsertId();
}
