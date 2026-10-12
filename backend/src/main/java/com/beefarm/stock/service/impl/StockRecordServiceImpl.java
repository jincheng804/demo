package com.beefarm.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.purchase.entity.Purchase;
import com.beefarm.purchase.mapper.PurchaseMapper;
import com.beefarm.sale.entity.Outbound;
import com.beefarm.sale.mapper.OutboundMapper;
import com.beefarm.stock.entity.Inventory;
import com.beefarm.stock.entity.StockRecord;
import com.beefarm.stock.mapper.InventoryMapper;
import com.beefarm.stock.mapper.StockRecordMapper;
import com.beefarm.stock.service.StockRecordService;
import com.beefarm.system.entity.SysUser;
import com.beefarm.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockRecordServiceImpl extends ServiceImpl<StockRecordMapper, StockRecord>
        implements StockRecordService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OutboundMapper outboundMapper;
    private final PurchaseMapper purchaseMapper;
    private final SysUserMapper sysUserMapper;
    private final InventoryMapper inventoryMapper;

    @Override
    public IPage<StockRecord> pageList(Integer pageNum, Integer pageSize, StockRecord query) {
        return baseMapper.selectRecordPage(new Page<>(pageNum, pageSize), query);
    }

    @Override
    public StockRecord detail(Long id) {
        StockRecord record = baseMapper.selectById(id);
        if (record == null) {
            return null;
        }
        // 物品名称
        if (record.getItemId() != null) {
            Inventory inv = inventoryMapper.selectById(record.getItemId());
            if (inv != null) {
                record.setItemName(inv.getItemName());
                record.setUnit(inv.getUnit());
            }
        }
        // 操作人真实姓名
        record.setOperatorName(resolveOperatorName(record.getOperator(), record.getRemark()));
        // 构建审批流程
        record.setFlowNodes(buildFlowNodes(record));
        return record;
    }

    /**
     * 解析操作人真实姓名：销售出库统一显示孙销售，其他关联sys_user
     */
    private String resolveOperatorName(String operator, String remark) {
        if (remark != null && remark.startsWith("销售出库")) {
            return "孙销售";
        }
        if (operator == null || operator.isEmpty()) {
            return "-";
        }
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, operator).last("limit 1"));
        return user != null ? user.getRealName() : operator;
    }

    /**
     * 构建审批流程节点
     */
    private List<StockRecord.FlowNode> buildFlowNodes(StockRecord record) {
        List<StockRecord.FlowNode> nodes = new ArrayList<>();
        String remark = record.getRemark() == null ? "" : record.getRemark();

        if (remark.startsWith("销售出库")) {
            // 销售出库：关联出库单
            String outboundNo = remark.substring("销售出库".length());
            Outbound outbound = outboundMapper.selectOne(new LambdaQueryWrapper<Outbound>()
                    .eq(Outbound::getOutboundNo, outboundNo).last("limit 1"));
            if (outbound != null) {
                record.setBizNo(outboundNo);
                boolean audited = outbound.getStatus() != null && outbound.getStatus() == 1;
                // 节点1：创建
                nodes.add(node("创建出库单", "孙销售",
                        outbound.getCreateTime() != null ? outbound.getCreateTime().format(FMT) : "-",
                        "销售员创建出库单，等待审核", 1));
                // 节点2：审核
                nodes.add(node("审核出库",
                        audited ? resolveName(outbound.getUpdateBy()) : "-",
                        audited && outbound.getUpdateTime() != null ? outbound.getUpdateTime().format(FMT) : "-",
                        audited ? "审核通过，扣减库存" : "待审核",
                        audited ? 1 : 0));
                // 节点3：归档
                nodes.add(node("出库归档",
                        audited ? "系统" : "-",
                        audited && outbound.getUpdateTime() != null ? outbound.getUpdateTime().format(FMT) : "-",
                        audited ? "出库单归档完成" : "待归档",
                        audited ? 1 : 0));
            } else {
                nodes.addAll(simpleFlow(record));
            }
        } else if (remark.startsWith("采购入库")) {
            // 采购入库：关联采购单
            String purchaseNo = remark.substring("采购入库".length());
            Purchase purchase = purchaseMapper.selectOne(new LambdaQueryWrapper<Purchase>()
                    .eq(Purchase::getPurchaseNo, purchaseNo).last("limit 1"));
            if (purchase != null) {
                record.setBizNo(purchaseNo);
                boolean audited = purchase.getStatus() != null && purchase.getStatus() == 1;
                nodes.add(node("创建采购单", resolveName(purchase.getCreateBy()),
                        purchase.getCreateTime() != null ? purchase.getCreateTime().format(FMT) : "-",
                        "采购员创建采购单，等待审核", 1));
                nodes.add(node("审核入库",
                        audited ? resolveName(purchase.getUpdateBy()) : "-",
                        audited && purchase.getUpdateTime() != null ? purchase.getUpdateTime().format(FMT) : "-",
                        audited ? "审核通过，增加库存" : "待审核",
                        audited ? 1 : 0));
                nodes.add(node("入库归档",
                        audited ? "系统" : "-",
                        audited && purchase.getUpdateTime() != null ? purchase.getUpdateTime().format(FMT) : "-",
                        audited ? "采购单归档完成" : "待归档",
                        audited ? 1 : 0));
            } else {
                nodes.addAll(simpleFlow(record));
            }
        } else {
            // 库存台账直接操作：无审核流程
            nodes.addAll(simpleFlow(record));
        }
        return nodes;
    }

    /**
     * 简单流程（库存台账直接操作）
     */
    private List<StockRecord.FlowNode> simpleFlow(StockRecord record) {
        List<StockRecord.FlowNode> nodes = new ArrayList<>();
        String time = record.getCreateTime() != null ? record.getCreateTime().format(FMT) : "-";
        String opName = resolveOperatorName(record.getOperator(), record.getRemark());
        String type = record.getBizType() == 1 ? "入库" : record.getBizType() == 2 ? "出库"
                : record.getBizType() == 3 ? "盘盈" : "盘亏";
        nodes.add(node("创建单据", opName, time, "库存台账直接" + type + "操作", 1));
        nodes.add(node("操作完成", opName, time, type + "操作完成，库存已更新", 1));
        nodes.add(node("归档", "系统", time, "流水自动归档", 1));
        return nodes;
    }

    private StockRecord.FlowNode node(String name, String operator, String time, String desc, Integer status) {
        StockRecord.FlowNode n = new StockRecord.FlowNode();
        n.setNodeName(name);
        n.setOperatorName(operator);
        n.setOperateTime(time);
        n.setDescription(desc);
        n.setStatus(status);
        return n;
    }

    private String resolveName(String username) {
        if (username == null || username.isEmpty()) return "-";
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username).last("limit 1"));
        return user != null ? user.getRealName() : username;
    }
}
