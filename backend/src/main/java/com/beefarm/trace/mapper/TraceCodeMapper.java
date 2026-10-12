package com.beefarm.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.trace.entity.TraceCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TraceCodeMapper extends BaseMapper<TraceCode> {
    @org.apache.ibatis.annotations.Update("UPDATE biz_trace_code SET scan_count=COALESCE(scan_count,0)+1, last_scan_time=CURRENT_TIMESTAMP WHERE id=#{id}")
    int incrementScan(@Param("id") Long id);

    IPage<TraceCode> selectTracePage(IPage<TraceCode> page, @Param("trace") TraceCode trace);
}
