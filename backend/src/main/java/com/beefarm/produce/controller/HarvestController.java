package com.beefarm.produce.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.produce.entity.Harvest;
import com.beefarm.produce.service.HarvestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "摇蜜采收")
@RestController
@RequestMapping("/harvest")
@RequiredArgsConstructor
public class HarvestController {

    private final HarvestService harvestService;

    @PreAuthorize("@ss.hasPermi('harvest:list')")
    @GetMapping("/list")
    public TableDataInfo<Harvest> list(Harvest query,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(harvestService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('harvest:add')")
    @PostMapping
    public R<String> add(@RequestBody Harvest harvest) {
        return R.ok("采收登记成功", harvestService.addHarvest(harvest));
    }

    @PreAuthorize("@ss.hasPermi('harvest:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Harvest harvest) {
        harvestService.updateHarvest(harvest);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('harvest:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        harvestService.deleteHarvest(id);
        return R.ok();
    }
}
