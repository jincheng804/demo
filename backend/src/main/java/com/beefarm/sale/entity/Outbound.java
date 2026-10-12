package com.beefarm.sale.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 出库单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_outbound")
public class Outbound extends AuditEntity {
    private String outboundNo;
    private Long customerId;
    private Integer totalQty;
    private BigDecimal totalAmount;
    private LocalDate outboundDate;
    private Integer status;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private List<OutboundItem> items;
}
