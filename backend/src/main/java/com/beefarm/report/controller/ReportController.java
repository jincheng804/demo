package com.beefarm.report.controller;

import com.beefarm.common.core.R;
import com.beefarm.report.mapper.ReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计报表：产量 / 销售 / 库存 / 利润
 */
@Tag(name = "统计报表")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportMapper reportMapper;

    @Operation(summary = "产量报表 dim=month|hive|source")
    @PreAuthorize("@ss.hasPermi('report:view')")
    @GetMapping("/production")
    public R<Map<String, Object>> production(@RequestParam(defaultValue = "month") String dim,
                                              @RequestParam(required = false) LocalDate beginTime,
                                              @RequestParam(required = false) LocalDate endTime) {
        List<Map<String, Object>> rows = reportMapper.selectProductionReport(dim, beginTime, endTime);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        result.put("total", rows.size());
        return R.ok(result);
    }

    @Operation(summary = "销售报表 dim=month|customer|product")
    @PreAuthorize("@ss.hasPermi('report:view')")
    @GetMapping("/sales")
    public R<Map<String, Object>> sales(@RequestParam(defaultValue = "month") String dim,
                                        @RequestParam(required = false) LocalDate beginTime,
                                        @RequestParam(required = false) LocalDate endTime) {
        List<Map<String, Object>> rows = reportMapper.selectSalesReport(dim, beginTime, endTime);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        result.put("total", rows.size());
        return R.ok(result);
    }

    @Operation(summary = "库存报表（按仓库/产品）")
    @PreAuthorize("@ss.hasPermi('report:view')")
    @GetMapping("/inventory")
    public R<Map<String, Object>> inventory() {
        List<Map<String, Object>> rows = reportMapper.selectInventoryReport();
        BigDecimal totalStock = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;
        for (Map<String, Object> row : rows) {
            totalStock = totalStock.add(toBD(row.get("stock")));
            totalValue = totalValue.add(toBD(row.get("amount")));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        result.put("totalStock", totalStock);
        result.put("totalValue", totalValue);
        return R.ok(result);
    }

    @Operation(summary = "利润报表：成本按类型 / 产值按月 / 毛利")
    @PreAuthorize("@ss.hasPermi('report:view')")
    @GetMapping("/profit")
    public R<Map<String, Object>> profit(@RequestParam(required = false) LocalDate beginTime,
                                          @RequestParam(required = false) LocalDate endTime) {
        Map<String, Object> summary = reportMapper.selectProfitSummary(beginTime, endTime);
        BigDecimal totalCost = toBD(summary == null ? null : summary.get("totalCost"));
        BigDecimal totalOutput = toBD(summary == null ? null : summary.get("totalOutput"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("costByType", reportMapper.selectCostByType(beginTime, endTime));
        result.put("outputByMonth", reportMapper.selectOutputByMonth(beginTime, endTime));
        result.put("totalCost", totalCost);
        result.put("totalOutput", totalOutput);
        result.put("grossProfit", totalOutput.subtract(totalCost));
        return R.ok(result);
    }

    private BigDecimal toBD(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(v.toString());
    }
}
