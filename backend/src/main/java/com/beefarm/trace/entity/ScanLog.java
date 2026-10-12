package com.beefarm.trace.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.beefarm.common.core.CreateOnlyEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 扫码日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_scan_log")
public class ScanLog extends CreateOnlyEntity {
    private String traceCode;
    private LocalDateTime scanTime;
    private String ip;
    private String deviceType;
    private String userAgent;
    /** 归属地(查询时由ip实时反查, 不落库) */
    @TableField(exist = false)
    private String location;
}
