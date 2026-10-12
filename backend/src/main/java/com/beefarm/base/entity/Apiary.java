package com.beefarm.base.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 蜂场分区
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_apiary")
public class Apiary extends AuditEntity {
    private String apiaryName;
    private String location;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String manager;
    private Long deptId;
    private Integer status;
}
