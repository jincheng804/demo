package com.beefarm.produce.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.produce.entity.ProductBatch;

public interface ProductBatchService extends IService<ProductBatch> {
    IPage<ProductBatch> pageList(Integer pageNum, Integer pageSize, ProductBatch query);

    /** 装瓶：生成产品批次，自动入库并更新采收批次状态 */
    String bottle(ProductBatch productBatch);

    /** 删除产品批次：回滚采收状态、回滚库存、逻辑删除 */
    void removeBatch(Long id);
}
