package com.beefarm.produce.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.produce.entity.Harvest;

public interface HarvestService extends IService<Harvest> {
    IPage<Harvest> pageList(Integer pageNum, Integer pageSize, Harvest query);

    /** 新增摇蜜采收，自动生成批次号并校验休药期 */
    String addHarvest(Harvest harvest);
    void updateHarvest(Harvest harvest);
    void deleteHarvest(Long id);
}
