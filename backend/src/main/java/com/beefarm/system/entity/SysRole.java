package com.beefarm.system.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 角色
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private String roleName;
    private String roleCode;
    private Integer dataScope;
    private Integer orderNum;
    private Integer status;

    /** 菜单ID集合 */
    @TableField(exist = false)
    private List<Long> menuIds;
}
