package com.beefarm.purchase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.purchase.entity.Supplier;

import java.time.LocalDate;

public interface SupplierService extends IService<Supplier> {

    /** 分页 + 多条件 + 日期范围 */
    IPage<Supplier> pageList(Integer pageNum, Integer pageSize, Supplier query,
                            LocalDate beginTime, LocalDate endTime);
}
