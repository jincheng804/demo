package com.beefarm.stock.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.stock.entity.StockRecord;

public interface StockRecordService extends IService<StockRecord> {
    IPage<StockRecord> pageList(Integer pageNum, Integer pageSize, StockRecord query);

    /**
     * 流水详情（含审批流程节点）
     */
    StockRecord detail(Long id);
}
