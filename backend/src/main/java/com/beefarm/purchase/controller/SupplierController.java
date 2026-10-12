package com.beefarm.purchase.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.common.utils.ExcelExportUtil;
import com.beefarm.purchase.entity.Supplier;
import com.beefarm.purchase.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "供应商")
@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "供应商分页")
    @PreAuthorize("@ss.hasPermi('supplier:list')")
    @GetMapping("/list")
    public TableDataInfo<Supplier> list(Supplier query,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) LocalDate beginTime,
                                       @RequestParam(required = false) LocalDate endTime) {
        return TableDataInfo.build(supplierService.pageList(pageNum, pageSize, query, beginTime, endTime));
    }

    @Operation(summary = "供应商详情")
    @PreAuthorize("@ss.hasPermi('supplier:list')")
    @GetMapping("/{id}")
    public R<Supplier> info(@PathVariable Long id) {
        return R.ok(supplierService.getById(id));
    }

    @Operation(summary = "新增供应商")
    @PreAuthorize("@ss.hasPermi('supplier:add')")
    @PostMapping
    public R<Void> add(@RequestBody Supplier supplier) {
        supplierService.save(supplier);
        return R.ok();
    }

    @Operation(summary = "修改供应商")
    @PreAuthorize("@ss.hasPermi('supplier:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Supplier supplier) {
        supplierService.updateById(supplier);
        return R.ok();
    }

    @Operation(summary = "删除供应商")
    @PreAuthorize("@ss.hasPermi('supplier:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        supplierService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "导出供应商")
    @PreAuthorize("@ss.hasPermi('supplier:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, Supplier query,
                       @RequestParam(required = false) LocalDate beginTime,
                       @RequestParam(required = false) LocalDate endTime) {
        List<Supplier> list = supplierService.pageList(1, 100000, query, beginTime, endTime).getRecords();
        String[] headers = {"供应商名称", "供应商类型", "联系人", "联系电话", "地址", "状态", "备注"};
        List<Object[]> rows = new ArrayList<>();
        for (Supplier s : list) {
            rows.add(new Object[]{
                    s.getSupplierName(), s.getSupplierType(), s.getContact(), s.getPhone(),
                    s.getAddress(),
                    s.getStatus() != null && s.getStatus() == 1 ? "合作中" : "停用",
                    s.getRemark()});
        }
        ExcelExportUtil.export(response, "供应商数据", "供应商信息表", headers, rows);
    }
}
