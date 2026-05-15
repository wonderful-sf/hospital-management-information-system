package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DepartmentMapper {
    List<Department> findAll(@Param("keyword") String keyword);
    Department findById(@Param("id") Long id);
    int insert(Department department);
    int update(Department department);
    int deleteById(@Param("id") Long id);
}
