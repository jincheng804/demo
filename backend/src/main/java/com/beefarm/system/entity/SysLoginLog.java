package com.beefarm.system.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String ip;
    private Integer status;
    private String msg;
    private LocalDateTime loginTime;
}
