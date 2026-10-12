package com.beefarm.base.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.base.entity.Hive;
import com.beefarm.base.service.HiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "蜂箱档案")
@RestController
@RequestMapping("/hive")
@RequiredArgsConstructor
public class HiveController {

    private final HiveService hiveService;

    @Operation(summary = "蜂箱分页")
    @PreAuthorize("@ss.hasPermi('hive:list')")
    @GetMapping("/list")
    public TableDataInfo<Hive> list(Hive query,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(hiveService.pageList(pageNum, pageSize, query));
    }

    @Operation(summary = "全部蜂箱(下拉)")
    @GetMapping("/all")
    public R<?> all() {
        return R.ok(hiveService.list());
    }

    @PreAuthorize("@ss.hasPermi('hive:list')")
    @GetMapping("/{id}")
    public R<Hive> info(@PathVariable Long id) {
        return R.ok(hiveService.getById(id));
    }

    @PreAuthorize("@ss.hasPermi('hive:add')")
    @PostMapping
    public R<Void> add(@RequestBody Hive hive) {
        hiveService.save(hive);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('hive:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Hive hive) {
        hiveService.updateById(hive);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('hive:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        hiveService.removeById(id);
        return R.ok();
    }
}
