package com.beefarm.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_supplier")
public class Supplier extends AuditEntity {
    private String supplierName;
    private String supplierType;
    private String contact;
    private String phone;
    private String address;
    private Integer status;
}
