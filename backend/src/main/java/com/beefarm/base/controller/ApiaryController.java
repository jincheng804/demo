package com.beefarm.base.controller;

import com.beefarm.common.core.R;
import com.beefarm.base.entity.Apiary;
import com.beefarm.base.service.ApiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "蜂场分区")
@RestController
@RequestMapping("/apiary")
@RequiredArgsConstructor
public class ApiaryController {

    private final ApiaryService apiaryService;

    @Operation(summary = "分区列表")
    @PreAuthorize("@ss.hasPermi('apiary:list')")
    @GetMapping("/list")
    public R<List<Apiary>> list() {
        return R.ok(apiaryService.list());
    }

    @Operation(summary = "分区详情")
    @PreAuthorize("@ss.hasPermi('apiary:list')")
    @GetMapping("/{id}")
    public R<Apiary> info(@PathVariable Long id) {
        return R.ok(apiaryService.getById(id));
    }

    @Operation(summary = "新增分区")
    @PreAuthorize("@ss.hasPermi('apiary:add')")
    @PostMapping
    public R<Void> add(@RequestBody Apiary apiary) {
        apiaryService.save(apiary);
        return R.ok();
    }

    @Operation(summary = "修改分区")
    @PreAuthorize("@ss.hasPermi('apiary:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Apiary apiary) {
        apiaryService.updateById(apiary);
        return R.ok();
    }

    @Operation(summary = "删除分区")
    @PreAuthorize("@ss.hasPermi('apiary:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        apiaryService.removeById(id);
        return R.ok();
    }
}
