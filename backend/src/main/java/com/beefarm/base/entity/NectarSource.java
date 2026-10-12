package com.beefarm.base.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 蜜源地
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nectar_source")
public class NectarSource extends AuditEntity {
    private String sourceName;
    private String plant;
    private String location;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private LocalDate bloomStart;
    private LocalDate bloomEnd;
    private Integer status;
}
