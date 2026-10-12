package com.beefarm.produce.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 摇蜜采收记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_harvest")
public class Harvest extends AuditEntity {
    private String batchNo;
    private Long hiveId;
    private Long sourceId;
    private LocalDate harvestDate;
    private String honeyType;
    private BigDecimal weight;
    private BigDecimal moisture;
    private String operator;
    private Integer status;

    @TableField(exist = false)
    private String hiveCode;
    @TableField(exist = false)
    private String sourceName;
}
