package com.beefarm.system.controller;

import com.beefarm.common.core.R;
import com.beefarm.report.mapper.ReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据看板：统计指标 / 趋势 / 分布（聚合走 ReportMapper 直查库）
 */
@Tag(name = "数据看板")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Map<Integer, String> HIVE_STATUS_NAME = Map.of(
            1, "正常蜂群", 2, "空箱", 3, "维修中", 4, "已淘汰");

    private final ReportMapper reportMapper;

    @Operation(summary = "看板核心指标")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1);
        Map<String, Object> db = reportMapper.selectDashboardStats(monthStart, monthEnd);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("hiveTotal", num(db, "hiveTotal"));
        map.put("activeHive", num(db, "activeHive"));
        map.put("monthHarvestWeight", num(db, "monthHarvestWeight"));
        map.put("monthOutboundBottles", num(db, "monthOutboundBottles"));
        map.put("warnCount", num(db, "warnCount"));
        map.put("todoCount", num(db, "todoCount"));
        map.put("scanTotal", num(db, "scanTotal"));
        map.put("totalWeight", num(db, "totalWeight"));
        map.put("totalBottles", num(db, "totalBottles"));
        map.put("outboundTotal", num(db, "outboundTotal"));
        map.put("productTotal", num(db, "productTotal"));
        map.put("harvestTotal", num(db, "harvestTotal"));
        return R.ok(map);
    }

    @Operation(summary = "近12月产量趋势")
    @GetMapping("/trend")
    public R<List<Map<String, Object>>> trend() {
        LocalDate begin = nearBegin();
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        for (Map<String, Object> row : reportMapper.selectHarvestTrend(begin, begin.plusMonths(12))) {
            data.put((String) row.get("month"), toBD(row.get("weight")));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String m : last12Months()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", m);
            item.put("weight", data.getOrDefault(m, BigDecimal.ZERO));
            result.add(item);
        }
        return R.ok(result);
    }

    @Operation(summary = "蜜种分布")
    @GetMapping("/honeyPie")
    public R<List<Map<String, Object>>> honeyPie() {
        return R.ok(reportMapper.selectHoneyPie());
    }

    @Operation(summary = "蜂箱状态分布")
    @GetMapping("/hiveStatus")
    public R<List<Map<String, Object>>> hiveStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : reportMapper.selectHiveStatus()) {
            Integer status = row.get("status") == null ? null
                    : ((Number) row.get("status")).intValue();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", HIVE_STATUS_NAME.getOrDefault(status, "其他"));
            item.put("value", num(row, "value"));
            result.add(item);
        }
        return R.ok(result);
    }

    @Operation(summary = "近12月出库趋势")
    @GetMapping("/outputTrend")
    public R<List<Map<String, Object>>> outputTrend() {
        LocalDate begin = nearBegin();
        Map<String, Map<String, Object>> data = new LinkedHashMap<>();
        for (Map<String, Object> row : reportMapper.selectOutputTrend(begin, begin.plusMonths(12))) {
            data.put((String) row.get("month"), row);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String m : last12Months()) {
            Map<String, Object> src = data.get(m);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", m);
            item.put("qty", src == null ? BigDecimal.ZERO : toBD(src.get("qty")));
            item.put("amount", src == null ? BigDecimal.ZERO : toBD(src.get("amount")));
            result.add(item);
        }
        return R.ok(result);
    }

    @Operation(summary = "近30天扫码趋势")
    @GetMapping("/scanTrend")
    public R<List<Map<String, Object>>> scanTrend() {
        LocalDate begin = LocalDate.now().minusDays(29);
        Map<String, Long> data = new LinkedHashMap<>();
        for (Map<String, Object> row : reportMapper.selectScanTrend(begin)) {
            data.put((String) row.get("date"), ((Number) row.get("count")).longValue());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate d = begin.plusDays(i);
            String key = d.format(DATE_FMT);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", key);
            item.put("count", data.getOrDefault(key, 0L));
            result.add(item);
        }
        return R.ok(result);
    }

    @Operation(summary = "库存预警Top")
    @GetMapping("/warnTop")
    public R<List<Map<String, Object>>> warnTop() {
        return R.ok(reportMapper.selectWarnTop());
    }

    // ---------------- 工具方法 ----------------

    /** 近12个月起始：当月向前推11个月的月初 */
    private LocalDate nearBegin() {
        return LocalDate.now().withDayOfMonth(1).minusMonths(11);
    }

    private List<String> last12Months() {
        List<String> months = new ArrayList<>();
        LocalDate m = nearBegin();
        for (int i = 0; i < 12; i++) {
            months.add(m.format(MONTH_FMT));
            m = m.plusMonths(1);
        }
        return months;
    }

    private Object num(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v == null ? 0 : v;
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
