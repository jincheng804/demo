package com.beefarm.breed.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.breed.entity.Inspection;

import java.time.LocalDate;

public interface InspectionService extends IService<Inspection> {

    /** 分页 + 多条件 + 日期范围 + 蜂箱编号 */
    IPage<Inspection> pageList(Integer pageNum, Integer pageSize, Inspection query,
                              LocalDate beginTime, LocalDate endTime);
}
