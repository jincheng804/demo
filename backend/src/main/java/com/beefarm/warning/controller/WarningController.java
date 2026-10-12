package com.beefarm.warning.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.warning.entity.Warning;
import com.beefarm.warning.service.WarningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 预警中心
 */
@Tag(name = "预警中心")
@RestController
@RequestMapping("/warning")
@RequiredArgsConstructor
public class WarningController {

    private final WarningService warningService;

    @Operation(summary = "预警分页列表")
    @PreAuthorize("@ss.hasPermi('warning:list')")
    @GetMapping("/list")
    public TableDataInfo<Warning> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String warningType,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Integer level) {
        return TableDataInfo.build(warningService.pageList(pageNum, pageSize, warningType, status, level));
    }

    @Operation(summary = "处理预警")
    @PreAuthorize("@ss.hasPermi('warning:handle')")
    @PostMapping("/handle/{id}")
    public R<Void> handle(@PathVariable Long id,
                          @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        warningService.handle(id, remark);
        return R.ok();
    }

    @Operation(summary = "按实时规则补录未处理预警")
    @PreAuthorize("@ss.hasPermi('warning:handle')")
    @PostMapping("/regenerate")
    public R<Map<String, Object>> regenerate() {
        int count = warningService.regenerate();
        return R.ok("补录完成，新增未处理预警 " + count + " 条", Map.of("inserted", count));
    }
}
