package com.beefarm.stock.entity;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出入库流水
 */
@Data
@TableName("biz_stock_record")
public class StockRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;
    private Long batchId;
    /** 所属仓库id（成品库=2） */
    private Long warehouseId;
    private Integer bizType;
    private BigDecimal quantity;
    private BigDecimal stockAfter;
    private String operator;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String remark;

    @TableField(exist = false)
    private String itemName;

    /** 单位 */
    @TableField(exist = false)
    private String unit;

    /** 操作人真实姓名 */
    @TableField(exist = false)
    private String operatorName;

    /** 审核人真实姓名 */
    @TableField(exist = false)
    private String auditorName;

    /** 关联业务单号 */
    @TableField(exist = false)
    private String bizNo;

    /** 审批流程节点 */
    @TableField(exist = false)
    private List<FlowNode> flowNodes;

    /**
     * 流程节点
     */
    @Data
    public static class FlowNode implements Serializable {
        /** 节点名称 */
        private String nodeName;
        /** 操作人真实姓名 */
        private String operatorName;
        /** 操作时间(精确到秒) */
        private String operateTime;
        /** 节点描述 */
        private String description;
        /** 状态(0未完成 1已完成) */
        private Integer status;
    }
}
