package com.beefarm.trace.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.beefarm.common.core.CreateOnlyEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 溯源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_trace_code")
public class TraceCode extends CreateOnlyEntity {
    private String traceCode;
    private Long productBatchId;
    private String qrUrl;
    private Integer scanCount;
    private LocalDateTime lastScanTime;

    @TableField(exist = false)
    private String productBatchNo;
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String spec;
}
