package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.InpatientRecord;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InpatientRecordMapper {
    List<InpatientRecord> findByAdmissionId(@Param("admissionId") Long admissionId);

    InpatientRecord findById(@Param("id") Long id);

    InpatientRecord findByAdmissionIdAndDate(@Param("admissionId") Long admissionId,
                                             @Param("recordDate") LocalDate recordDate);

    int insert(InpatientRecord record);

    int update(InpatientRecord record);
}
