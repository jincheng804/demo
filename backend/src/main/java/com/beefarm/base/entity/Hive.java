package com.beefarm.base.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 蜂箱档案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_hive")
public class Hive extends AuditEntity {
    private String hiveCode;
    private Long apiaryId;
    private String boxType;
    private String location;
    private LocalDate entryDate;
    private Integer status;

    @TableField(exist = false)
    private String apiaryName;
}
