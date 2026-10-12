package com.beefarm.breed.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.breed.entity.Queen;

public interface QueenService extends IService<Queen> {
    IPage<Queen> pageList(Integer pageNum, Integer pageSize, Queen query);
}
