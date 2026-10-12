package com.beefarm.breed.controller;

import com.beefarm.breed.entity.Breeding;
import com.beefarm.breed.service.BreedingService;
import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "分蜂繁育")
@RestController
@RequestMapping("/breeding")
@RequiredArgsConstructor
public class BreedingController {

    private final BreedingService breedingService;

    @Operation(summary = "分蜂分页")
    @PreAuthorize("@ss.hasPermi('breeding:list')")
    @GetMapping("/list")
    public TableDataInfo<Breeding> list(Breeding query,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) LocalDate beginTime,
                                       @RequestParam(required = false) LocalDate endTime) {
        return TableDataInfo.build(breedingService.pageList(pageNum, pageSize, query, beginTime, endTime));
    }

    @Operation(summary = "分蜂详情")
    @PreAuthorize("@ss.hasPermi('breeding:list')")
    @GetMapping("/{id}")
    public R<Breeding> info(@PathVariable Long id) {
        return R.ok(breedingService.getById(id));
    }

    @Operation(summary = "新增分蜂")
    @PreAuthorize("@ss.hasPermi('breeding:add')")
    @PostMapping
    public R<Void> add(@RequestBody Breeding breeding) {
        breedingService.save(breeding);
        return R.ok();
    }

    @Operation(summary = "修改分蜂")
    @PreAuthorize("@ss.hasPermi('breeding:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Breeding breeding) {
        breedingService.updateById(breeding);
        return R.ok();
    }

    @Operation(summary = "删除分蜂")
    @PreAuthorize("@ss.hasPermi('breeding:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        breedingService.removeById(id);
        return R.ok();
    }
}
