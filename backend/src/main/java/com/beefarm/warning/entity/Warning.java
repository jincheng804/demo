package com.beefarm.warning.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 预警
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_warning")
public class Warning extends AuditEntity {
    private String warningType;
    private Long bizId;
    private String content;
    private Integer level;
    private Integer status;
    private LocalDateTime generateTime;
    private String handleBy;
    private LocalDateTime handleTime;
}
