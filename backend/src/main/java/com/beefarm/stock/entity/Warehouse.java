package com.beefarm.stock.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓库
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_warehouse")
public class Warehouse extends AuditEntity {
    private String whName;
    private String whType;
    private String location;
    private String manager;
    private String phone;
    private Integer status;
}
