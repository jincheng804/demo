package com.beefarm.sale.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.sale.entity.Outbound;

public interface OutboundService extends IService<Outbound> {

    IPage<Outbound> pageList(Integer pageNum, Integer pageSize, Outbound query);

    /** 新增出库单(含明细) */
    String createOutbound(Outbound outbound);

    /** 获取出库单详情(含明细) */
    Outbound detail(Long id);

    /** 审核出库：扣减库存、更新批次状态 */
    void audit(Long id);
    void deleteDraft(Long id);
}
