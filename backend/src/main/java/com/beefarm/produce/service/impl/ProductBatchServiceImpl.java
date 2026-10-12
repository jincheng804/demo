package com.beefarm.produce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.exception.BusinessException;
import com.beefarm.common.utils.SecurityUtils;
import com.beefarm.produce.entity.Harvest;
import com.beefarm.produce.entity.ProductBatch;
import com.beefarm.produce.mapper.HarvestMapper;
import com.beefarm.produce.mapper.ProductBatchMapper;
import com.beefarm.produce.service.ProductBatchService;
import com.beefarm.stock.entity.Inventory;
import com.beefarm.stock.entity.StockRecord;
import com.beefarm.stock.entity.Warehouse;
import com.beefarm.stock.mapper.InventoryMapper;
import com.beefarm.stock.mapper.StockRecordMapper;
import com.beefarm.stock.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ProductBatchServiceImpl extends ServiceImpl<ProductBatchMapper, ProductBatch>
        implements ProductBatchService {
    private final com.beefarm.common.service.BusinessGuard businessGuard;

    private final HarvestMapper harvestMapper;
    private final InventoryMapper inventoryMapper;
    private final StockRecordMapper stockRecordMapper;
    private final WarehouseMapper warehouseMapper;

    /** 读取实际配置的成品库。 */
    private Long finishedWarehouseId() {
        Warehouse finished = warehouseMapper.selectOne(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getWhType, "成品库")
                .last("limit 1"));
        com.beefarm.common.utils.BusinessChecks.require(finished != null, "请先配置成品库");
        return finished.getId();
    }

    @Override
    public IPage<ProductBatch> pageList(Integer pageNum, Integer pageSize, ProductBatch query) {
        return baseMapper.selectProductPage(new Page<>(pageNum, pageSize), query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String bottle(ProductBatch p) {
        businessGuard.lock();
        if (p.getHarvestId() == null) {
            throw new BusinessException("请选择来源采收批次");
        }
        Harvest harvest = harvestMapper.selectById(p.getHarvestId());
        if (harvest == null) {
            throw new BusinessException("来源采收批次不存在");
        }
        if (p.getProduceDate() == null) {
            p.setProduceDate(LocalDate.now());
        }
        if (p.getProductName() == null) {
            p.setProductName(harvest.getHoneyType());
        }
        com.beefarm.common.utils.BusinessChecks.require(p.getId() == null, "新增批次不能指定ID");
        com.beefarm.common.utils.BusinessChecks.require(Integer.valueOf(1).equals(harvest.getStatus()), "该采收批次已装瓶或不可装瓶");
        com.beefarm.common.utils.BusinessChecks.require(p.getBottleCount() != null && p.getBottleCount() > 0, "装瓶数量必须大于0");
        com.beefarm.common.utils.BusinessChecks.require(java.util.Set.of("250g", "500g", "1000g").contains(p.getSpec() == null ? "" : p.getSpec()), "请选择250g、500g或1000g规格");
        BigDecimal grams = new BigDecimal(p.getSpec().replace("g", ""));
        com.beefarm.common.utils.BusinessChecks.require(harvest.getWeight() != null && grams.multiply(BigDecimal.valueOf(p.getBottleCount()))
                .compareTo(harvest.getWeight().multiply(BigDecimal.valueOf(1000))) <= 0, "装瓶总重量不能超过采收重量");
        com.beefarm.common.utils.BusinessChecks.require(!p.getProduceDate().isBefore(harvest.getHarvestDate()), "生产日期不能早于采收日期");
        com.beefarm.common.utils.BusinessChecks.money(p.getPrice() == null ? BigDecimal.ZERO : p.getPrice());
        p.setQcStatus(0);
        p.setStatus(1);
        String batchNo = businessGuard.nextNumber("CP", p.getProduceDate());
        p.setProductBatchNo(batchNo);
        baseMapper.insert(p);

        // 更新采收批次状态为已装瓶
        harvest.setStatus(2);
        harvestMapper.updateById(harvest);

        // 产品入库：按 产品名称+规格+成品库 维护台账
        int bottles = p.getBottleCount() == null ? 0 : p.getBottleCount();
        if (bottles > 0) {
            Long whId = finishedWarehouseId();
            Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getItemType, 2)
                    .eq(Inventory::getWarehouseId, whId)
                    .eq(Inventory::getItemName, p.getProductName())
                    .eq(Inventory::getSpec, p.getSpec()));
            if (inventory == null) {
                inventory = new Inventory();
                inventory.setItemType(2);
                inventory.setWarehouseId(whId);
                inventory.setItemName(p.getProductName());
                inventory.setSpec(p.getSpec());
                inventory.setUnit("瓶");
                inventory.setStock(BigDecimal.ZERO);
                inventory.setWarnStock(BigDecimal.TEN);
                inventoryMapper.insert(inventory);
            }
            BigDecimal after = inventory.getStock().add(BigDecimal.valueOf(bottles));
            inventory.setStock(after);
            inventoryMapper.updateById(inventory);

            StockRecord record = new StockRecord();
            record.setItemId(inventory.getId());
            record.setBatchId(p.getId());
            record.setWarehouseId(whId);
            record.setBizType(1);
            record.setQuantity(BigDecimal.valueOf(bottles));
            record.setStockAfter(after);
            record.setRemark("装瓶入库 " + batchNo);
            record.setOperator(SecurityUtils.getUsername());
            stockRecordMapper.insert(record);
        }
        return batchNo;
    }

    /**
     * 删除产品批次：回滚采收状态、回滚库存、记录出库流水、逻辑删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(Long id) {
        businessGuard.lock();
        ProductBatch p = baseMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("产品批次不存在");
        }
        com.beefarm.common.utils.BusinessChecks.require(
                baseMapper.countDependencies(id) == 0, "该批次已有出库、质检或溯源码记录，不能删除");
        // 1. 回滚采收批次状态为待装瓶
        if (p.getHarvestId() != null) {
            Harvest harvest = harvestMapper.selectById(p.getHarvestId());
            if (harvest != null) {
                harvest.setStatus(1);
                harvestMapper.updateById(harvest);
            }
        }
        // 2. 回滚库存
        int bottles = p.getBottleCount() == null ? 0 : p.getBottleCount();
        if (bottles > 0) {
            Long whId = finishedWarehouseId();
            Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getItemType, 2)
                    .eq(Inventory::getWarehouseId, whId)
                    .eq(Inventory::getItemName, p.getProductName())
                    .eq(Inventory::getSpec, p.getSpec()));
            com.beefarm.common.utils.BusinessChecks.require(inventory != null, "库存台账缺失，不能删除批次");
            if (inventory != null) {
                BigDecimal after = inventory.getStock().subtract(BigDecimal.valueOf(bottles));
                com.beefarm.common.utils.BusinessChecks.require(after.signum() >= 0, "库存已被使用，不能回滚该批次");
                inventory.setStock(after);
                inventoryMapper.updateById(inventory);
                // 记录出库流水
                StockRecord record = new StockRecord();
                record.setItemId(inventory.getId());
                record.setBatchId(p.getId());
                record.setWarehouseId(whId);
                record.setBizType(2);
                record.setQuantity(BigDecimal.valueOf(-bottles));
                record.setStockAfter(after);
                record.setRemark("删除批次回滚 " + p.getProductBatchNo());
                record.setOperator(SecurityUtils.getUsername());
                stockRecordMapper.insert(record);
            }
        }
        // 3. 逻辑删除
        baseMapper.deleteById(id);
    }
}
