package com.beefarm.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beefarm.trace.entity.ScanLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScanLogMapper extends BaseMapper<ScanLog> {
    @org.apache.ibatis.annotations.Select("SELECT COALESCE(NULLIF(device_type,''),'其他') AS name, COUNT(*) AS value " +
        "FROM biz_scan_log GROUP BY COALESCE(NULLIF(device_type,''),'其他') ORDER BY value DESC")
    java.util.List<com.beefarm.trace.entity.ScanDeviceStat> deviceStats();
}
