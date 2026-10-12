package com.beefarm.stock.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.common.utils.ExcelExportUtil;
import com.beefarm.stock.entity.Warehouse;
import com.beefarm.stock.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "仓库管理")
@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "仓库分页")
    @PreAuthorize("@ss.hasPermi('warehouse:list')")
    @GetMapping("/list")
    public TableDataInfo<Warehouse> list(Warehouse query,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) LocalDate beginTime,
                                         @RequestParam(required = false) LocalDate endTime) {
        return TableDataInfo.build(warehouseService.pageList(
                pageNum, pageSize, query, beginTime, endTime));
    }

    @Operation(summary = "全部仓库(下拉)")
    @GetMapping("/all")
    public R<List<Warehouse>> all() {
        return R.ok(warehouseService.list());
    }

    @Operation(summary = "仓库详情")
    @PreAuthorize("@ss.hasPermi('warehouse:list')")
    @GetMapping("/{id}")
    public R<Warehouse> info(@PathVariable Long id) {
        return R.ok(warehouseService.getById(id));
    }

    @Operation(summary = "新增仓库")
    @PreAuthorize("@ss.hasPermi('warehouse:add')")
    @PostMapping
    public R<Void> add(@RequestBody Warehouse warehouse) {
        warehouseService.save(warehouse);
        return R.ok();
    }

    @Operation(summary = "修改仓库")
    @PreAuthorize("@ss.hasPermi('warehouse:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Warehouse warehouse) {
        warehouseService.updateById(warehouse);
        return R.ok();
    }

    @Operation(summary = "删除仓库")
    @PreAuthorize("@ss.hasPermi('warehouse:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        warehouseService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "导出仓库")
    @PreAuthorize("@ss.hasPermi('warehouse:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, Warehouse query,
                       @RequestParam(required = false) LocalDate beginTime,
                       @RequestParam(required = false) LocalDate endTime) {
        List<Warehouse> list = warehouseService.pageList(
                1, 100000, query, beginTime, endTime).getRecords();
        String[] headers = {"仓库名称", "仓库类型", "位置", "负责人", "联系电话", "状态", "备注"};
        List<Object[]> rows = new ArrayList<>();
        for (Warehouse w : list) {
            rows.add(new Object[]{
                    w.getWhName(), w.getWhType(), w.getLocation(), w.getManager(),
                    w.getPhone(), w.getStatus() != null && w.getStatus() == 1 ? "正常" : "停用",
                    w.getRemark()});
        }
        ExcelExportUtil.export(response, "仓库数据", "仓库信息表", headers, rows);
    }
}
