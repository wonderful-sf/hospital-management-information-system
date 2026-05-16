package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.DoctorSchedule;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DoctorScheduleMapper {
    List<DoctorSchedule> findAll(@Param("doctorId") Long doctorId, @Param("departmentId") Long departmentId);

    DoctorSchedule findById(@Param("id") Long id);

    int insert(DoctorSchedule doctorSchedule);

    int update(DoctorSchedule doctorSchedule);

    int deleteById(@Param("id") Long id);

    Long lastInsertId();

    int countConflicts(@Param("doctorId") Long doctorId,
                       @Param("startTime") LocalDateTime startTime,
                       @Param("endTime") LocalDateTime endTime,
                       @Param("excludeId") Long excludeId);
}
