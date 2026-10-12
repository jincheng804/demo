package com.beefarm.disease.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.disease.entity.Disease;
import com.beefarm.disease.service.DiseaseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "病害记录")
@RestController
@RequestMapping("/disease")
@RequiredArgsConstructor
public class DiseaseController {

    private final DiseaseService diseaseService;

    @PreAuthorize("@ss.hasPermi('disease:list')")
    @GetMapping("/list")
    public TableDataInfo<Disease> list(Disease query,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(diseaseService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('disease:add')")
    @PostMapping
    public R<Void> add(@RequestBody Disease disease) {
        diseaseService.save(disease);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('disease:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Disease disease) {
        diseaseService.updateById(disease);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('disease:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        diseaseService.removeById(id);
        return R.ok();
    }
}
