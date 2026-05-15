package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Medicine;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MedicineMapper {
    List<Medicine> findAll(@Param("keyword") String keyword);

    Optional<Medicine> findById(@Param("id") Long id);

    int insert(Medicine medicine);

    int update(Medicine medicine);

    int disableById(@Param("id") Long id);
}
