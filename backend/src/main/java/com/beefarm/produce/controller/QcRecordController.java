package com.beefarm.produce.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.produce.entity.QcLog;
import com.beefarm.produce.entity.QcRecord;
import com.beefarm.produce.service.QcRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "质检记录")
@RestController
@RequestMapping("/qc")
@RequiredArgsConstructor
public class QcRecordController {

    private final QcRecordService qcRecordService;

    @PreAuthorize("@ss.hasPermi('qc:list')")
    @GetMapping("/list")
    public TableDataInfo<QcRecord> list(QcRecord query,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(qcRecordService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('qc:add')")
    @PostMapping
    public R<Void> add(@RequestBody QcRecord qc) {
        qcRecordService.addQc(qc);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('qc:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody QcRecord qc) {
        qcRecordService.updateQcWithLog(qc);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('qc:list')")
    @GetMapping("/log/{qcId}")
    public R<List<QcLog>> getLogs(@PathVariable Long qcId) {
        return R.ok(qcRecordService.getQcLogs(qcId));
    }
}
