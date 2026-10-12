package com.beefarm.breed.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.breed.entity.Feeding;

import java.time.LocalDate;

public interface FeedingService extends IService<Feeding> {

    /** 分页 + 多条件 + 日期范围 + 蜂箱编号 */
    IPage<Feeding> pageList(Integer pageNum, Integer pageSize, Feeding query,
                           LocalDate beginTime, LocalDate endTime);
}
