package com.beefarm.sale.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_customer")
public class Customer extends AuditEntity {
    private String customerName;
    private String contact;
    private String phone;
    private String address;
}
