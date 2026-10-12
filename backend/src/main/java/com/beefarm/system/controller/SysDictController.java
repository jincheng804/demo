package com.beefarm.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.system.entity.SysDictData;
import com.beefarm.system.entity.SysDictType;
import com.beefarm.system.mapper.SysDictDataMapper;
import com.beefarm.system.mapper.SysDictTypeMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典管理")
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictTypeMapper typeMapper;
    private final SysDictDataMapper dataMapper;

    @PreAuthorize("@ss.hasPermi('dict:list')")
    @GetMapping("/type/list")
    public TableDataInfo<SysDictType> typeList(SysDictType query,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysDictType>(pageNum, pageSize);
        return TableDataInfo.build(typeMapper.selectPage(page,
                new LambdaQueryWrapper<SysDictType>()
                        .like(query.getDictName() != null, SysDictType::getDictName, query.getDictName())
                        .like(query.getDictType() != null, SysDictType::getDictType, query.getDictType())));
    }

    @PreAuthorize("@ss.hasPermi('dict:list')")
    @GetMapping("/data/list")
    public R<List<SysDictData>> dataList(@RequestParam String dictType) {
        return R.ok(dataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getOrderNum)));
    }

    @PostMapping("/type")
    @PreAuthorize("@ss.hasPermi('dict:list')")
    public R<Void> addType(@RequestBody SysDictType type) {
        typeMapper.insert(type);
        return R.ok();
    }

    @PutMapping("/type")
    @PreAuthorize("@ss.hasPermi('dict:list')")
    public R<Void> editType(@RequestBody SysDictType type) {
        typeMapper.updateById(type);
        return R.ok();
    }

    @DeleteMapping("/type/{id}")
    @PreAuthorize("@ss.hasPermi('dict:list')")
    public R<Void> removeType(@PathVariable Long id) {
        typeMapper.deleteById(id);
        return R.ok();
    }

    @PostMapping("/data")
    @PreAuthorize("@ss.hasPermi('dict:list')")
    public R<Void> addData(@RequestBody SysDictData data) {
        dataMapper.insert(data);
        return R.ok();
    }

    @PutMapping("/data")
    @PreAuthorize("@ss.hasPermi('dict:list')")
    public R<Void> editData(@RequestBody SysDictData data) {
        dataMapper.updateById(data);
        return R.ok();
    }

    @DeleteMapping("/data/{id}")
    @PreAuthorize("@ss.hasPermi('dict:list')")
    public R<Void> removeData(@PathVariable Long id) {
        dataMapper.deleteById(id);
        return R.ok();
    }
}
