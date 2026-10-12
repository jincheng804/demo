package com.beefarm.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 采购明细
 */
@Data
@TableName("biz_purchase_item")
public class PurchaseItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long purchaseId;
    private String materialName;
    private String spec;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private BigDecimal amount;
}
