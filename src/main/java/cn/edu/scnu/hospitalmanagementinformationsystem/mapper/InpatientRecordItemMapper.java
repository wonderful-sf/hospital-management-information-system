package cn.edu.scnu.hospitalmanagementinformationsystem.mapper;

import cn.edu.scnu.hospitalmanagementinformationsystem.entity.InpatientRecordItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InpatientRecordItemMapper {
    List<InpatientRecordItem> findByInpatientRecordId(@Param("inpatientRecordId") Long inpatientRecordId);

    int insert(InpatientRecordItem item);
}
