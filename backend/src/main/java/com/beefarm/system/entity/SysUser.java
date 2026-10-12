package com.beefarm.system.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.TableField;
import com.beefarm.common.core.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends AuditEntity {

    private Long deptId;
    private String username;
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String password;
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    private Integer authVersion;
    private String realName;
    private String phone;
    private String email;
    private Integer gender;
    private String avatar;
    private Integer status;
    private String loginIp;
    private LocalDateTime loginDate;

    /** 角色ID集合 */
    @TableField(exist = false)
    private List<Long> roleIds;

    /** 部门名称 */
    @TableField(exist = false)
    private String deptName;
}
