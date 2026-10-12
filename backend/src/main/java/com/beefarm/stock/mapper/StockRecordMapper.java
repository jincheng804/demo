package com.beefarm.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.stock.entity.StockRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockRecordMapper extends BaseMapper<StockRecord> {
    // Historical imports stored outgoing quantities as positive numbers; direction is authoritative.
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(SUM(CASE WHEN biz_type IN (2,4) THEN -ABS(quantity) ELSE ABS(quantity) END),0) FROM biz_stock_record WHERE batch_id=#{id}")
    java.math.BigDecimal availableBatch(@org.apache.ibatis.annotations.Param("id") Long id);

    IPage<StockRecord> selectRecordPage(IPage<StockRecord> page, @Param("record") StockRecord record);
}
