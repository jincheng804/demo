package com.beefarm.base.controller;

import com.beefarm.common.core.R;
import com.beefarm.base.entity.NectarSource;
import com.beefarm.base.service.NectarSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "蜜源地")
@RestController
@RequestMapping("/source")
@RequiredArgsConstructor
public class NectarSourceController {

    private final NectarSourceService sourceService;

    @PreAuthorize("@ss.hasPermi('source:list')")
    @GetMapping("/list")
    public R<List<NectarSource>> list() {
        return R.ok(sourceService.list());
    }

    @PreAuthorize("@ss.hasPermi('source:list')")
    @GetMapping("/{id}")
    public R<NectarSource> info(@PathVariable Long id) {
        return R.ok(sourceService.getById(id));
    }

    @PreAuthorize("@ss.hasPermi('source:add')")
    @PostMapping
    public R<Void> add(@RequestBody NectarSource source) {
        sourceService.save(source);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('source:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody NectarSource source) {
        sourceService.updateById(source);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('source:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        sourceService.removeById(id);
        return R.ok();
    }
}
