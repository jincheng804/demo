package com.beefarm.stock.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.stock.entity.Warehouse;

import java.time.LocalDate;

public interface WarehouseService extends IService<Warehouse> {

    /** 分页 + 多条件 + 日期范围 */
    IPage<Warehouse> pageList(Integer pageNum, Integer pageSize, Warehouse query,
                              LocalDate beginTime, LocalDate endTime);
}
