package com.beefarm.common.mapper;
import org.apache.ibatis.annotations.*;
@Mapper
public interface BusinessGuardMapper {
    @Select("SELECT id FROM sys_business_lock WHERE id = 1 FOR UPDATE")
    Integer lock();
    @Select("SELECT COALESCE(MAX(CAST(RIGHT(no, 4) AS DECIMAL(10,0))), 0) FROM (" +
        "SELECT batch_no AS no FROM biz_harvest UNION ALL " +
        "SELECT product_batch_no AS no FROM biz_product_batch UNION ALL " +
        "SELECT outbound_no AS no FROM biz_outbound UNION ALL " +
        "SELECT purchase_no AS no FROM biz_purchase) numbers " +
        "WHERE no LIKE CONCAT(#{prefix}, '%') AND CHAR_LENGTH(no) = CHAR_LENGTH(#{prefix}) + 4")
    Integer maxSequence(@Param("prefix") String prefix);
}
