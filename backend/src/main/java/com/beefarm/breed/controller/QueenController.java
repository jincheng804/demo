package com.beefarm.breed.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.breed.entity.Queen;
import com.beefarm.breed.service.QueenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "蜂王档案")
@RestController
@RequestMapping("/queen")
@RequiredArgsConstructor
public class QueenController {

    private final QueenService queenService;

    @PreAuthorize("@ss.hasPermi('queen:list')")
    @GetMapping("/list")
    public TableDataInfo<Queen> list(Queen query,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(queenService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('queen:add')")
    @PostMapping
    public R<Void> add(@RequestBody Queen queen) {
        queenService.save(queen);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('queen:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Queen queen) {
        queenService.updateById(queen);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('queen:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        queenService.removeById(id);
        return R.ok();
    }
}
