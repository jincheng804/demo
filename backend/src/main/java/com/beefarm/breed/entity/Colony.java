package com.beefarm.breed.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 蜂群日常记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_colony")
public class Colony extends AuditEntity {
    private Long hiveId;
    private LocalDate recordDate;
    private Integer colonyStrength;
    private Integer broodFrame;
    private Integer honeyFrame;
    private String beeCount;

    @TableField(exist = false)
    private String hiveCode;
}
