package com.beefarm.breed.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.breed.entity.Breeding;

import java.time.LocalDate;

public interface BreedingService extends IService<Breeding> {

    /** 分页 + 多条件 + 日期范围 + 蜂箱编号 */
    IPage<Breeding> pageList(Integer pageNum, Integer pageSize, Breeding query,
                            LocalDate beginTime, LocalDate endTime);
}
