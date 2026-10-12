package com.beefarm.breed.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.breed.entity.Colony;
import com.beefarm.breed.service.ColonyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "蜂群记录")
@RestController
@RequestMapping("/colony")
@RequiredArgsConstructor
public class ColonyController {

    private final ColonyService colonyService;

    @PreAuthorize("@ss.hasPermi('colony:list')")
    @GetMapping("/list")
    public TableDataInfo<Colony> list(Colony query,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(colonyService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('colony:add')")
    @PostMapping
    public R<Void> add(@RequestBody Colony colony) {
        colonyService.save(colony);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('colony:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Colony colony) {
        colonyService.updateById(colony);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('colony:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        colonyService.removeById(id);
        return R.ok();
    }
}
