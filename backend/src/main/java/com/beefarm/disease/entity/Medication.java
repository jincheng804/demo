package com.beefarm.disease.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用药记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_medication")
public class Medication extends AuditEntity {
    private Long hiveId;
    private Long diseaseId;
    private String drugName;
    private String dose;
    private LocalDate useDate;
    private Integer withdrawalDays;
    private LocalDate safeDate;
    private String operator;

    @TableField(exist = false)
    private String hiveCode;
}
