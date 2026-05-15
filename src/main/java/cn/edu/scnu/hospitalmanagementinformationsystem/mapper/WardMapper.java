package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Ward;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WardMapper {
    List<Ward> findAll(@Param("keyword") String keyword);

    Optional<Ward> findById(@Param("id") Long id);

    int insert(Ward ward);

    int update(Ward ward);

    int deleteById(@Param("id") Long id);
}
