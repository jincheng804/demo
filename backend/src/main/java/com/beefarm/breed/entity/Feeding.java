package com.beefarm.breed.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 饲喂记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_feeding")
public class Feeding extends AuditEntity {
    private Long hiveId;
    private LocalDate feedDate;
    private String feedType;
    private BigDecimal amount;
    private String unit;
    private String operator;

    @TableField(exist = false)
    private String hiveCode;
}
