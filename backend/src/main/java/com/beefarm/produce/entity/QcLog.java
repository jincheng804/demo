package com.beefarm.produce.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_qc_log")
public class QcLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long qcId;
    private String fieldName;
    private String fieldLabel;
    private String oldValue;
    private String newValue;
    private String operator;
    private LocalDateTime createTime;
}
