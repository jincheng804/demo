package com.beefarm.breed.controller;

import com.beefarm.breed.entity.Inspection;
import com.beefarm.breed.service.InspectionService;
import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "巡检记录")
@RestController
@RequestMapping("/inspection")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    @Operation(summary = "巡检分页")
    @PreAuthorize("@ss.hasPermi('inspection:list')")
    @GetMapping("/list")
    public TableDataInfo<Inspection> list(Inspection query,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) LocalDate beginTime,
                                         @RequestParam(required = false) LocalDate endTime) {
        return TableDataInfo.build(inspectionService.pageList(pageNum, pageSize, query, beginTime, endTime));
    }

    @Operation(summary = "巡检详情")
    @PreAuthorize("@ss.hasPermi('inspection:list')")
    @GetMapping("/{id}")
    public R<Inspection> info(@PathVariable Long id) {
        return R.ok(inspectionService.getById(id));
    }

    @Operation(summary = "新增巡检")
    @PreAuthorize("@ss.hasPermi('inspection:add')")
    @PostMapping
    public R<Void> add(@RequestBody Inspection inspection) {
        inspectionService.save(inspection);
        return R.ok();
    }

    @Operation(summary = "修改巡检")
    @PreAuthorize("@ss.hasPermi('inspection:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Inspection inspection) {
        inspectionService.updateById(inspection);
        return R.ok();
    }

    @Operation(summary = "删除巡检")
    @PreAuthorize("@ss.hasPermi('inspection:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        inspectionService.removeById(id);
        return R.ok();
    }
}
