package com.beefarm.purchase.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.common.utils.ExcelExportUtil;
import com.beefarm.purchase.entity.Purchase;
import com.beefarm.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "采购单")
@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "采购单分页")
    @PreAuthorize("@ss.hasPermi('purchase:list')")
    @GetMapping("/list")
    public TableDataInfo<Purchase> list(Purchase query,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) LocalDate beginTime,
                                      @RequestParam(required = false) LocalDate endTime) {
        return TableDataInfo.build(purchaseService.pageList(pageNum, pageSize, query, beginTime, endTime));
    }

    @Operation(summary = "采购单详情(含明细)")
    @PreAuthorize("@ss.hasPermi('purchase:list')")
    @GetMapping("/{id}")
    public R<Purchase> detail(@PathVariable Long id) {
        return R.ok(purchaseService.detail(id));
    }

    @Operation(summary = "新增采购单(含明细)")
    @PreAuthorize("@ss.hasPermi('purchase:add')")
    @PostMapping
    public R<String> add(@RequestBody Purchase purchase) {
        return R.ok("采购单创建成功", purchaseService.createPurchase(purchase));
    }

    @Operation(summary = "修改采购单(含明细)")
    @PreAuthorize("@ss.hasPermi('purchase:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Purchase purchase) {
        purchaseService.updatePurchase(purchase);
        return R.ok();
    }

    @Operation(summary = "删除采购单")
    @PreAuthorize("@ss.hasPermi('purchase:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return R.ok();
    }

    @Operation(summary = "审核采购单(入库)")
    @PreAuthorize("@ss.hasPermi('purchase:audit')")
    @PostMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id) {
        purchaseService.audit(id);
        return R.ok();
    }

    @Operation(summary = "导出采购单")
    @PreAuthorize("@ss.hasPermi('purchase:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, Purchase query,
                       @RequestParam(required = false) LocalDate beginTime,
                       @RequestParam(required = false) LocalDate endTime) {
        List<Purchase> list = purchaseService.pageList(1, 100000, query, beginTime, endTime).getRecords();
        String[] headers = {"采购单号", "供应商", "入库仓库", "采购日期", "数量", "金额", "状态", "操作人", "备注"};
        List<Object[]> rows = new ArrayList<>();
        for (Purchase p : list) {
            rows.add(new Object[]{
                    p.getPurchaseNo(), p.getSupplierName(), p.getWarehouseName(), p.getPurchaseDate(),
                    p.getTotalQty(), p.getTotalAmount(),
                    p.getStatus() != null && p.getStatus() == 1 ? "已入库" : "待审核",
                    p.getOperator(), p.getRemark()});
        }
        ExcelExportUtil.export(response, "采购单数据", "采购单信息表", headers, rows);
    }
}
