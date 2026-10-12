package com.beefarm.breed.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 分蜂繁育
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_breeding")
public class Breeding extends AuditEntity {
    private Long hiveId;
    private String breedType;
    private LocalDate breedDate;
    private Long sourceHiveId;
    private Long targetHiveId;
    private Integer quantity;

    @TableField(exist = false)
    private String hiveCode;
    @TableField(exist = false)
    private String sourceHiveCode;
    @TableField(exist = false)
    private String targetHiveCode;
}
