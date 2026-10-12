package com.beefarm.disease.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 病害记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_disease")
public class Disease extends AuditEntity {
    private Long hiveId;
    private String diseaseType;
    private LocalDate diseaseDate;
    private Integer level;
    private Integer handleStatus;

    @TableField(exist = false)
    private String hiveCode;
}
