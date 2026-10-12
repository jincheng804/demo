package com.beefarm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.system.entity.SysDept;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {
    List<SysDept> treeList();

    void saveDept(SysDept dept);
}
