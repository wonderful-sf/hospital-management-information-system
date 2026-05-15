package cn.edu.scnu.hospitalmanagementinformationsystem.service;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.Department;
import cn.edu.scnu.hospitalmanagementinformationsystem.mapper.DepartmentMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService {
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    public List<Department> findAll(String keyword) {
        return departmentMapper.findAll(keyword);
    }

    public Department findById(Long id) {
        Department department = departmentMapper.findById(id);
        if (department == null) {
            throw new RuntimeException("科室不存在");
        }
        return department;
    }

    public Department create(Department department) {
        departmentMapper.insert(department);
        return department;
    }

    public Department update(Long id, Department department) {
        findById(id);
        Department updated = new Department(id, department.name(), department.location());
        departmentMapper.update(updated);
        return updated;
    }

    public void delete(Long id) {
        findById(id);
        departmentMapper.deleteById(id);
    }
}
