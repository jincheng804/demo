package com.beefarm.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beefarm.base.entity.Hive;
import com.beefarm.base.mapper.HiveMapper;
import com.beefarm.common.utils.ExcelExportUtil;
import com.beefarm.produce.entity.Harvest;
import com.beefarm.produce.entity.ProductBatch;
import com.beefarm.produce.mapper.HarvestMapper;
import com.beefarm.produce.mapper.ProductBatchMapper;
import com.beefarm.sale.entity.Outbound;
import com.beefarm.sale.mapper.OutboundMapper;
import com.beefarm.stock.entity.Inventory;
import com.beefarm.stock.mapper.InventoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一数据导出：蜂箱/采收/产品批次/出库/库存
 */
@Tag(name = "数据导出")
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final HiveMapper hiveMapper;
    private final HarvestMapper harvestMapper;
    private final ProductBatchMapper productBatchMapper;
    private final OutboundMapper outboundMapper;
    private final InventoryMapper inventoryMapper;

    @Operation(summary = "导出蜂箱档案")
    @PreAuthorize("@ss.hasPermi('hive:list')")
    @GetMapping("/hive")
    public void exportHive(HttpServletResponse response) {
        List<Hive> list = hiveMapper.selectHivePage(new Page<>(1, 100000), new Hive()).getRecords();
        String[] headers = {"蜂箱编号", "所属分区", "蜂箱型号", "摆放位置", "入栏日期", "状态", "备注"};
        List<Object[]> rows = new ArrayList<>();
        for (Hive h : list) {
            rows.add(new Object[]{h.getHiveCode(), h.getApiaryName(), h.getBoxType(), h.getLocation(),
                    h.getEntryDate(), hiveStatusName(h.getStatus()), h.getRemark()});
        }
        ExcelExportUtil.export(response, "蜂箱档案", "蜂箱档案表", headers, rows);
    }

    @Operation(summary = "导出采收记录")
    @PreAuthorize("@ss.hasPermi('harvest:list')")
    @GetMapping("/harvest")
    public void exportHarvest(HttpServletResponse response) {
        List<Harvest> list = harvestMapper.selectHarvestPage(new Page<>(1, 100000), new Harvest()).getRecords();
        String[] headers = {"采收批次号", "蜂箱编号", "蜜源地", "采收日期", "蜜种", "重量(kg)", "波美度", "操作人", "状态"};
        List<Object[]> rows = new ArrayList<>();
        for (Harvest h : list) {
            rows.add(new Object[]{h.getBatchNo(), h.getHiveCode(), h.getSourceName(), h.getHarvestDate(),
                    h.getHoneyType(), h.getWeight(), h.getMoisture(), h.getOperator(),
                    h.getStatus() != null && h.getStatus() == 2 ? "已装瓶" : "待加工"});
        }
        ExcelExportUtil.export(response, "采收记录", "摇蜜采收记录表", headers, rows);
    }

    @Operation(summary = "导出产品批次")
    @PreAuthorize("@ss.hasPermi('product:list')")
    @GetMapping("/product")
    public void exportProduct(HttpServletResponse response) {
        List<ProductBatch> list = productBatchMapper.selectProductPage(new Page<>(1, 100000), new ProductBatch()).getRecords();
        String[] headers = {"产品批次号", "采收批次", "产品名称", "规格", "装瓶数量", "生产日期", "质检状态", "状态"};
        List<Object[]> rows = new ArrayList<>();
        for (ProductBatch p : list) {
            rows.add(new Object[]{p.getProductBatchNo(), p.getHarvestBatchNo(), p.getProductName(), p.getSpec(),
                    p.getBottleCount(), p.getProduceDate(), qcName(p.getQcStatus()),
                    p.getStatus() != null && p.getStatus() == 2 ? "已出库" : "在库"});
        }
        ExcelExportUtil.export(response, "产品批次", "产品批次表", headers, rows);
    }

    @Operation(summary = "导出出库单")
    @PreAuthorize("@ss.hasPermi('outbound:list')")
    @GetMapping("/outbound")
    public void exportOutbound(HttpServletResponse response) {
        List<Outbound> list = outboundMapper.selectOutboundPage(new Page<>(1, 100000), new Outbound()).getRecords();
        String[] headers = {"出库单号", "客户", "总数量", "总金额(元)", "出库日期", "状态"};
        List<Object[]> rows = new ArrayList<>();
        for (Outbound o : list) {
            rows.add(new Object[]{o.getOutboundNo(), o.getCustomerName(), o.getTotalQty(),
                    o.getTotalAmount(), o.getOutboundDate(), outboundStatusName(o.getStatus())});
        }
        ExcelExportUtil.export(response, "出库单", "出库单表", headers, rows);
    }

    @Operation(summary = "导出库存台账")
    @PreAuthorize("@ss.hasPermi('inventory:list')")
    @GetMapping("/inventory")
    public void exportInventory(HttpServletResponse response) {
        List<Inventory> list = inventoryMapper.selectPage(new Page<>(1, 100000), null).getRecords();
        String[] headers = {"类型", "物品名称", "规格", "单位", "库存数量", "预警库存"};
        List<Object[]> rows = new ArrayList<>();
        for (Inventory i : list) {
            rows.add(new Object[]{i.getItemType() != null && i.getItemType() == 2 ? "产品" : "蜂具耗材",
                    i.getItemName(), i.getSpec(), i.getUnit(), i.getStock(), i.getWarnStock()});
        }
        ExcelExportUtil.export(response, "库存台账", "库存台账表", headers, rows);
    }

    private String hiveStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "正常";
            case 2 -> "空箱";
            case 3 -> "维修";
            case 4 -> "淘汰";
            default -> "未知";
        };
    }

    private String qcName(Integer qcStatus) {
        if (qcStatus == null) {
            return "未检";
        }
        return switch (qcStatus) {
            case 1 -> "合格";
            case 2 -> "不合格";
            default -> "未检";
        };
    }

    private String outboundStatusName(Integer status) {
        if (status == null) {
            return "待审核";
        }
        return switch (status) {
            case 1 -> "已出库";
            case 2 -> "已取消";
            default -> "待审核";
        };
    }
}
