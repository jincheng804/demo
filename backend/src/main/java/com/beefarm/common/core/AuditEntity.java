package com.beefarm.common.core;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

/**
 * 带逻辑删除的实体基类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditEntity extends BaseEntity {

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer deleted;
}
