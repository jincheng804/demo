package com.beefarm.base.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.base.entity.Hive;

public interface HiveService extends IService<Hive> {
    IPage<Hive> pageList(Integer pageNum, Integer pageSize, Hive query);
}
