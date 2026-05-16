package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.PrepaidRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PrepaidRecordMapper {
    List<PrepaidRecord> findByAdmissionId(@Param("admissionId") Long admissionId);

    PrepaidRecord findById(@Param("id") Long id);

    int insert(PrepaidRecord record);
}
