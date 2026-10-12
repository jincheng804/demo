package com.beefarm.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 成本费用
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_expense")
public class Expense extends AuditEntity {
    private Long apiaryId;
    private LocalDate expenseDate;
    private String expenseType;
    private BigDecimal amount;
    private String summary;

    @TableField(exist = false)
    private String apiaryName;
}
