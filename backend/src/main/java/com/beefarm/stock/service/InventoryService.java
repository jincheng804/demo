package com.beefarm.stock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.stock.entity.Inventory;

import java.math.BigDecimal;

public interface InventoryService extends IService<Inventory> {
    void createItem(Inventory item);
    void updateItem(Inventory item);
    void deleteItem(Long id);

    /** 出入库操作并记录流水 */
    void stockOp(Long itemId, Integer bizType, BigDecimal quantity, String remark);
}
