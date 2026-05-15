package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Bed;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BedMapper {
    List<Bed> findAll(@Param("keyword") String keyword);

    Optional<Bed> findById(@Param("id") Long id);

    int insert(Bed bed);

    int update(Bed bed);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
