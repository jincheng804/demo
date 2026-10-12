package com.beefarm.system.controller;

import com.beefarm.common.core.R;
import com.beefarm.system.entity.SysDept;
import com.beefarm.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @PreAuthorize("@ss.hasPermi('dept:list')")
    @GetMapping("/list")
    public R<List<SysDept>> list() {
        return R.ok(deptService.treeList());
    }

    @PreAuthorize("@ss.hasPermi('dept:add')")
    @PostMapping
    public R<Void> add(@RequestBody SysDept dept) {
        deptService.saveDept(dept);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('dept:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody SysDept dept) {
        deptService.saveDept(dept);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('dept:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        deptService.removeById(id);
        return R.ok();
    }
}
