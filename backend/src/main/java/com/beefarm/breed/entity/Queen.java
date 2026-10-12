package com.beefarm.breed.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 蜂王档案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_queen")
public class Queen extends AuditEntity {
    private String queenCode;
    private Long hiveId;
    private String breed;
    private LocalDate birthDate;
    private Integer status;

    @TableField(exist = false)
    private String hiveCode;
}
