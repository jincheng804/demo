package com.beefarm.system.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单/权限
 */
@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String menuName;
    /** M目录 C菜单 F按钮 */
    private String menuType;
    private String perms;
    private String path;
    private String component;
    private String icon;
    private Integer orderNum;
    private Integer visible;
    private Integer status;
    private LocalDateTime createTime;

    /** 子菜单 */
    @TableField(exist = false)
    private List<SysMenu> children;
}
