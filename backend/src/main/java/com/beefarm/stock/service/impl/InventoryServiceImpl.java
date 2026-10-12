package com.beefarm.stock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.exception.BusinessException;
import com.beefarm.common.utils.SecurityUtils;
import com.beefarm.stock.entity.Inventory;
import com.beefarm.stock.entity.StockRecord;
import com.beefarm.stock.mapper.InventoryMapper;
import com.beefarm.stock.mapper.StockRecordMapper;
import com.beefarm.stock.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory>
        implements InventoryService {
    private final com.beefarm.common.service.BusinessGuard businessGuard;

    private final StockRecordMapper stockRecordMapper;
    private final com.beefarm.stock.mapper.WarehouseMapper warehouseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockOp(Long itemId, Integer bizType, BigDecimal quantity, String remark) {
        businessGuard.lock();
        com.beefarm.common.utils.BusinessChecks.quantity(quantity);
        Inventory item = baseMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("库存物品不存在");
        }
        com.beefarm.common.utils.BusinessChecks.require(!Integer.valueOf(2).equals(item.getItemType()), "成品库存请通过装瓶和销售出库操作，以保留批次追溯");
        BigDecimal current = item.getStock() == null ? BigDecimal.ZERO : item.getStock();
        BigDecimal after;
        com.beefarm.common.utils.BusinessChecks.require(bizType != null, "请选择库存操作类型");
        switch (bizType) {
            case 1, 3 -> after = current.add(quantity);   // 入库/盘盈
            case 2, 4 -> {                              // 出库/盘亏
                if (current.compareTo(quantity) < 0) {
                    throw new BusinessException("库存不足，当前库存 " + current + item.getUnit());
                }
                after = current.subtract(quantity);
            }
            default -> throw new BusinessException("未知的操作类型");
        }
        com.beefarm.common.utils.BusinessChecks.money(after);
        item.setStock(after);
        baseMapper.updateById(item);

        // 流水(出库/盘亏按负数记录)
        StockRecord record = new StockRecord();
        record.setItemId(itemId);
        record.setWarehouseId(item.getWarehouseId());
        record.setBizType(bizType);
        record.setQuantity((bizType == 2 || bizType == 4) ? quantity.negate() : quantity);
        record.setStockAfter(after);
        record.setRemark(remark);
        record.setOperator(SecurityUtils.getUsername());
        stockRecordMapper.insert(record);
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void createItem(Inventory item) {
        businessGuard.lock();
        com.beefarm.common.utils.BusinessChecks.require(item.getId() == null, "新增库存不能指定ID");
        com.beefarm.common.utils.BusinessChecks.require(Integer.valueOf(1).equals(item.getItemType()), "成品台账由装瓶流程自动建立");
        com.beefarm.common.utils.BusinessChecks.require(item.getWarehouseId() != null && warehouseMapper.selectById(item.getWarehouseId()) != null, "请选择有效仓库");
        com.beefarm.common.utils.BusinessChecks.require(item.getItemName() != null && !item.getItemName().isBlank(), "请输入物品名称");
        item.setSpec(item.getSpec() == null ? "" : item.getSpec().trim());
        com.beefarm.common.utils.BusinessChecks.require(baseMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, item.getWarehouseId()).eq(Inventory::getItemType, 1)
                .eq(Inventory::getItemName, item.getItemName()).eq(Inventory::getSpec, item.getSpec())) == 0, "该仓库已有相同物品和规格");
        item.setStock(BigDecimal.ZERO);
        if (item.getWarnStock() == null) item.setWarnStock(BigDecimal.TEN);
        com.beefarm.common.utils.BusinessChecks.require(item.getWarnStock().signum() >= 0, "预警库存不能为负数");
        baseMapper.insert(item);
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void updateItem(Inventory item) {
        businessGuard.lock();
        com.beefarm.common.utils.BusinessChecks.require(item.getId() != null && baseMapper.selectById(item.getId()) != null, "库存物品不存在");
        com.beefarm.common.utils.BusinessChecks.require(item.getWarnStock() != null && item.getWarnStock().signum() >= 0, "请输入有效预警库存");
        Inventory update = new Inventory();
        update.setId(item.getId()); update.setWarnStock(item.getWarnStock()); update.setRemark(item.getRemark());
        baseMapper.updateById(update);
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        businessGuard.lock();
        Inventory item = baseMapper.selectById(id);
        com.beefarm.common.utils.BusinessChecks.require(item != null && (item.getStock() == null || item.getStock().signum() == 0), "有库存的物品不能删除");
        com.beefarm.common.utils.BusinessChecks.require(stockRecordMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockRecord>().eq(StockRecord::getItemId, id)) == 0, "已有库存流水，不能删除台账");
        baseMapper.deleteById(id);
    }

}
