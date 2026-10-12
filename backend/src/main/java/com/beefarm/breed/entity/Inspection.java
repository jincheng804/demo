package com.beefarm.breed.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 蜂箱巡检
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inspection")
public class Inspection extends AuditEntity {
    private Long hiveId;
    private LocalDate inspectDate;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String colonyPotential;
    private String queenStatus;
    private String feedStatus;
    private String abnormal;
    private String inspector;

    @TableField(exist = false)
    private String hiveCode;
}
