package com.beefarm.stock.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 库存台账
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inventory")
public class Inventory extends AuditEntity {
    private Integer itemType;
    private String itemName;
    private String spec;
    private String unit;
    private BigDecimal stock;
    private BigDecimal warnStock;
    /** 所属仓库id（成品库=2） */
    private Long warehouseId;
}
