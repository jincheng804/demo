package com.beefarm.stock.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.stock.entity.StockRecord;
import com.beefarm.stock.service.StockRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "出入库流水")
@RestController
@RequestMapping("/stockrecord")
@RequiredArgsConstructor
public class StockRecordController {

    private final StockRecordService stockRecordService;

    @PreAuthorize("@ss.hasPermi('stockrecord:list')")
    @GetMapping("/list")
    public TableDataInfo<StockRecord> list(StockRecord query,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(stockRecordService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('stockrecord:list')")
    @GetMapping("/{id}")
    public R<StockRecord> detail(@PathVariable Long id) {
        return R.ok(stockRecordService.detail(id));
    }
}
