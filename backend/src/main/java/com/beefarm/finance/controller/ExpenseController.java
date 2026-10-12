package com.beefarm.finance.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.common.utils.ExcelExportUtil;
import com.beefarm.finance.entity.Expense;
import com.beefarm.finance.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 成本费用
 */
@Tag(name = "成本费用")
@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "费用分页")
    @PreAuthorize("@ss.hasPermi('expense:list')")
    @GetMapping("/list")
    public TableDataInfo<Expense> list(Expense query,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) LocalDate beginTime,
                                       @RequestParam(required = false) LocalDate endTime) {
        return TableDataInfo.build(expenseService.pageList(pageNum, pageSize, query, beginTime, endTime));
    }

    @Operation(summary = "费用详情")
    @PreAuthorize("@ss.hasPermi('expense:list')")
    @GetMapping("/{id}")
    public R<Expense> info(@PathVariable Long id) {
        return R.ok(expenseService.getById(id));
    }

    @Operation(summary = "新增费用")
    @PreAuthorize("@ss.hasPermi('expense:add')")
    @PostMapping
    public R<Void> add(@RequestBody Expense expense) {
        expenseService.save(expense);
        return R.ok();
    }

    @Operation(summary = "修改费用")
    @PreAuthorize("@ss.hasPermi('expense:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Expense expense) {
        expenseService.updateById(expense);
        return R.ok();
    }

    @Operation(summary = "删除费用")
    @PreAuthorize("@ss.hasPermi('expense:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        expenseService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "导出费用")
    @PreAuthorize("@ss.hasPermi('expense:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, Expense query,
                       @RequestParam(required = false) LocalDate beginTime,
                       @RequestParam(required = false) LocalDate endTime) {
        List<Expense> list = expenseService.pageList(1, 100000, query, beginTime, endTime).getRecords();
        String[] headers = {"费用日期", "费用类型", "所属蜂场", "金额(元)", "摘要", "备注"};
        List<Object[]> rows = new ArrayList<>();
        for (Expense e : list) {
            rows.add(new Object[]{
                    e.getExpenseDate(), e.getExpenseType(), e.getApiaryName(),
                    e.getAmount(), e.getSummary(), e.getRemark()});
        }
        ExcelExportUtil.export(response, "成本费用", "成本费用明细表", headers, rows);
    }
}
