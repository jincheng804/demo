package com.beefarm.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_purchase")
public class Purchase extends AuditEntity {
    private String purchaseNo;
    private Long supplierId;
    private Long warehouseId;
    private LocalDate purchaseDate;
    private BigDecimal totalQty;
    private BigDecimal totalAmount;
    private Integer status;
    private String operator;

    @TableField(exist = false)
    private String supplierName;
    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private List<PurchaseItem> items;
}
