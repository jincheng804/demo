package com.beefarm.disease.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.disease.entity.Medication;
import com.beefarm.disease.service.MedicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用药记录")
@RestController
@RequestMapping("/medication")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @PreAuthorize("@ss.hasPermi('medication:list')")
    @GetMapping("/list")
    public TableDataInfo<Medication> list(Medication query,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(medicationService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('medication:add')")
    @PostMapping
    public R<Void> add(@RequestBody Medication medication) {
        medicationService.saveMedication(medication);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('medication:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Medication medication) {
        medicationService.saveMedication(medication);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('medication:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        medicationService.removeById(id);
        return R.ok();
    }
}
