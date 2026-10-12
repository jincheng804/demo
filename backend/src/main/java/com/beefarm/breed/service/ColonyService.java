package com.beefarm.breed.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.breed.entity.Colony;

public interface ColonyService extends IService<Colony> {
    IPage<Colony> pageList(Integer pageNum, Integer pageSize, Colony query);
}
