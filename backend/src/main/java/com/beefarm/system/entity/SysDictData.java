package com.beefarm.system.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据
 */
@Data
@TableName("sys_dict_data")
public class SysDictData implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private String cssClass;
    private Integer orderNum;
    private Integer status;
    private LocalDateTime createTime;
}
