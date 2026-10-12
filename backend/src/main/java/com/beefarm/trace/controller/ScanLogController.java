package com.beefarm.trace.controller;

import com.beefarm.common.core.TableDataInfo;
import com.beefarm.trace.entity.ScanLog;
import com.beefarm.trace.service.ScanLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "扫码日志")
@RestController
@RequestMapping("/scanlog")
@RequiredArgsConstructor
public class ScanLogController {

    private final ScanLogService scanLogService;
    private final com.beefarm.trace.mapper.ScanLogMapper scanLogMapper;

    @PreAuthorize("@ss.hasPermi('scanlog:list')")
    @GetMapping("/deviceStats")
    public com.beefarm.common.core.R<java.util.List<com.beefarm.trace.entity.ScanDeviceStat>> deviceStats() {
        return com.beefarm.common.core.R.ok(scanLogMapper.deviceStats());
    }


    @Operation(summary = "扫码日志分页(只读)")
    @PreAuthorize("@ss.hasPermi('scanlog:list')")
    @GetMapping("/list")
    public TableDataInfo<ScanLog> list(ScanLog query,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) LocalDate beginTime,
                                     @RequestParam(required = false) LocalDate endTime) {
        return TableDataInfo.build(scanLogService.pageList(pageNum, pageSize, query, beginTime, endTime));
    }
}
