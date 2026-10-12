package com.beefarm.breed.controller;

import com.beefarm.breed.entity.Feeding;
import com.beefarm.breed.service.FeedingService;
import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "饲喂记录")
@RestController
@RequestMapping("/feeding")
@RequiredArgsConstructor
public class FeedingController {

    private final FeedingService feedingService;

    @Operation(summary = "饲喂分页")
    @PreAuthorize("@ss.hasPermi('feeding:list')")
    @GetMapping("/list")
    public TableDataInfo<Feeding> list(Feeding query,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) LocalDate beginTime,
                                      @RequestParam(required = false) LocalDate endTime) {
        return TableDataInfo.build(feedingService.pageList(pageNum, pageSize, query, beginTime, endTime));
    }

    @Operation(summary = "饲喂详情")
    @PreAuthorize("@ss.hasPermi('feeding:list')")
    @GetMapping("/{id}")
    public R<Feeding> info(@PathVariable Long id) {
        return R.ok(feedingService.getById(id));
    }

    @Operation(summary = "新增饲喂")
    @PreAuthorize("@ss.hasPermi('feeding:add')")
    @PostMapping
    public R<Void> add(@RequestBody Feeding feeding) {
        feedingService.save(feeding);
        return R.ok();
    }

    @Operation(summary = "修改饲喂")
    @PreAuthorize("@ss.hasPermi('feeding:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Feeding feeding) {
        feedingService.updateById(feeding);
        return R.ok();
    }

    @Operation(summary = "删除饲喂")
    @PreAuthorize("@ss.hasPermi('feeding:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        feedingService.removeById(id);
        return R.ok();
    }
}
