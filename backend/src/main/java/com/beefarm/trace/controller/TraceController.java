package com.beefarm.trace.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.trace.entity.TraceCode;
import com.beefarm.trace.service.TraceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "溯源管理")
@RestController
@RequestMapping("/trace")
@RequiredArgsConstructor
public class TraceController {

    private final TraceService traceService;

    @PreAuthorize("@ss.hasPermi('trace:list')")
    @GetMapping("/list")
    public TableDataInfo<TraceCode> list(TraceCode query,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(traceService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('trace:add')")
    @PostMapping("/generate/{productBatchId}")
    public R<TraceCode> generate(@PathVariable Long productBatchId) {
        return R.ok(traceService.generate(productBatchId));
    }

    @PreAuthorize("@ss.hasPermi('trace:view')")
    @GetMapping("/code/{traceCode}")
    public R<Map<String, Object>> detail(@PathVariable String traceCode) {
        return R.ok(traceService.publicTrace(traceCode, null));
    }

    @PreAuthorize("@ss.hasPermi('trace:view')")
    @GetMapping(value = "/qr/{traceCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qr(@PathVariable String traceCode) {
        return traceService.qrImage(traceCode);
    }

    // ============ 公开接口(免登录) ============

    @GetMapping("/public/{traceCode}")
    public R<Map<String, Object>> publicTrace(@PathVariable String traceCode, HttpServletRequest request) {
        return R.ok(traceService.publicTrace(traceCode, request));
    }

    @GetMapping(value = "/public/qr/{traceCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] publicQr(@PathVariable String traceCode) {
        return traceService.qrImage(traceCode);
    }
}
