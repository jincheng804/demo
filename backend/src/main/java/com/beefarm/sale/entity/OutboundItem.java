package com.beefarm.sale.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 出库明细
 */
@Data
@TableName("biz_outbound_item")
public class OutboundItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long outboundId;
    private Long productBatchId;
    private Integer quantity;
    private BigDecimal price;

    /** 展示用 */
    private transient String productBatchNo;
    private transient String productName;
    private transient String spec;
}
