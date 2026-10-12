package com.beefarm.purchase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.purchase.entity.Purchase;

import java.time.LocalDate;

public interface PurchaseService extends IService<Purchase> {

    /** 分页 + 多条件 + 日期范围 + 供应商/仓库名称 */
    IPage<Purchase> pageList(Integer pageNum, Integer pageSize, Purchase query,
                            LocalDate beginTime, LocalDate endTime);

    /** 新增采购单(含明细) */
    String createPurchase(Purchase purchase);

    /** 采购单详情(含明细) */
    Purchase detail(Long id);

    /** 修改采购单(含明细) */
    void updatePurchase(Purchase purchase);

    /** 删除采购单(含明细) */
    void deletePurchase(Long id);

    /** 审核采购单：蜂具耗材入库、写库存流水 */
    void audit(Long id);
}
