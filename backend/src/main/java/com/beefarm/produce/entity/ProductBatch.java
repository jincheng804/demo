package com.beefarm.produce.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 产品批次
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_product_batch")
public class ProductBatch extends AuditEntity {
    private String productBatchNo;
    private Long harvestId;
    private String productName;
    private String spec;
    private Integer bottleCount;
    private BigDecimal price;
    private LocalDate produceDate;
    private Integer shelfLife;
    private Integer qcStatus;
    private Integer status;

    @TableField(exist = false)
    private String harvestBatchNo;
    @TableField(exist = false)
    private String honeyType;
    @TableField(exist = false)
    private BigDecimal availableBottles;
}
