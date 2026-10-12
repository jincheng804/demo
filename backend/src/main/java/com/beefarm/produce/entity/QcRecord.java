package com.beefarm.produce.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 质检记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_qc_record")
public class QcRecord extends AuditEntity {
    private Long batchId;
    private String qcItems;
    private Integer qcResult;
    private String qcValue;
    private String inspector;
    private LocalDate qcDate;

    @TableField(exist = false)
    private String productBatchNo;

    @TableField(exist = false)
    private String productName;
}
