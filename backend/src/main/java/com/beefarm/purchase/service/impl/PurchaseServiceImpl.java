package com.beefarm.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.exception.BusinessException;
import com.beefarm.purchase.entity.Purchase;
import com.beefarm.purchase.entity.PurchaseItem;
import com.beefarm.purchase.mapper.PurchaseItemMapper;
import com.beefarm.purchase.mapper.PurchaseMapper;
import com.beefarm.purchase.service.PurchaseService;
import com.beefarm.stock.entity.Inventory;
import com.beefarm.stock.entity.StockRecord;
import com.beefarm.stock.mapper.InventoryMapper;
import com.beefarm.stock.mapper.StockRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl extends ServiceImpl<PurchaseMapper, Purchase>
        implements PurchaseService {
    private final com.beefarm.common.service.BusinessGuard businessGuard;

    private final PurchaseItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;
    private final StockRecordMapper stockRecordMapper;
    private final com.beefarm.stock.mapper.WarehouseMapper warehouseMapper;
    private void checkWarehouse(Long id) {
        com.beefarm.common.utils.BusinessChecks.require(id != null && warehouseMapper.selectById(id) != null, "请选择有效入库仓库");
    }

    @Override
    public IPage<Purchase> pageList(Integer pageNum, Integer pageSize, Purchase query,
                                   LocalDate beginTime, LocalDate endTime) {
        return baseMapper.selectPurchasePage(new Page<>(pageNum, pageSize), query, beginTime, endTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPurchase(Purchase p) {
        businessGuard.lock();
        com.beefarm.common.utils.BusinessChecks.require(p.getId() == null, "新增采购单不能指定ID");
        if (p.getItems() == null || p.getItems().isEmpty()) {
            throw new BusinessException("请添加采购明细");
        }
        if (p.getWarehouseId() == null) {
            throw new BusinessException("请选择入库仓库");
        }
        if (p.getPurchaseDate() == null) {
            p.setPurchaseDate(LocalDate.now());
        }
        // 采购单号 CG + yyyyMMdd + 4位流水
        String no = businessGuard.nextNumber("CG", p.getPurchaseDate());
        p.setPurchaseNo(no);
        p.setStatus(0);
        checkWarehouse(p.getWarehouseId());

        // 汇总
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseItem item : p.getItems()) {
            if (item.getQuantity() == null
                    || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("采购数量必须大于0");
            }
            if (item.getPrice() == null) {
                item.setPrice(BigDecimal.ZERO);
            }
            com.beefarm.common.utils.BusinessChecks.money(item.getPrice());
            com.beefarm.common.utils.BusinessChecks.quantity(item.getQuantity());
            com.beefarm.common.utils.BusinessChecks.require(item.getMaterialName() != null && !item.getMaterialName().isBlank(), "请填写物料名称");
            item.setId(null);
            BigDecimal amount = item.getPrice().multiply(item.getQuantity()).setScale(2, java.math.RoundingMode.HALF_UP);
            item.setAmount(amount);
            totalQty = totalQty.add(item.getQuantity());
            totalAmount = totalAmount.add(amount);
        }
        com.beefarm.common.utils.BusinessChecks.quantity(totalQty);
        com.beefarm.common.utils.BusinessChecks.money(totalAmount);
        p.setTotalQty(totalQty);
        p.setTotalAmount(totalAmount);
        baseMapper.insert(p);

        for (PurchaseItem item : p.getItems()) {
            item.setPurchaseId(p.getId());
            itemMapper.insert(item);
        }
        return no;
    }

    @Override
    public Purchase detail(Long id) {
        Purchase p = baseMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("采购单不存在");
        }
        p.setItems(itemMapper.selectItemsByPurchaseId(id));
        return p;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchase(Purchase p) {
        businessGuard.lock();
        if (p.getId() == null) {
            throw new BusinessException("采购单id不能为空");
        }
        Purchase db = baseMapper.selectById(p.getId());
        if (db == null) {
            throw new BusinessException("采购单不存在");
        }
        if (db.getStatus() != null && db.getStatus() == 1) {
            throw new BusinessException("已审核的采购单不能修改");
        }
        if (p.getWarehouseId() == null) p.setWarehouseId(db.getWarehouseId());
        checkWarehouse(p.getWarehouseId());
        p.setStatus(db.getStatus());
        p.setPurchaseNo(db.getPurchaseNo());
        com.beefarm.common.utils.BusinessChecks.require(p.getItems() != null && !p.getItems().isEmpty(), "请添加采购明细");
        // 重新汇总明细
        if (p.getItems() != null && !p.getItems().isEmpty()) {
            BigDecimal totalQty = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (PurchaseItem item : p.getItems()) {
                com.beefarm.common.utils.BusinessChecks.quantity(item.getQuantity());
                com.beefarm.common.utils.BusinessChecks.money(item.getPrice());
                com.beefarm.common.utils.BusinessChecks.require(item.getMaterialName() != null && !item.getMaterialName().isBlank(), "请填写物料名称");
                if (item.getPrice() == null) {
                    item.setPrice(BigDecimal.ZERO);
                }
                if (item.getQuantity() != null) {
                    item.setAmount(item.getPrice().multiply(item.getQuantity()).setScale(2, java.math.RoundingMode.HALF_UP));
                    totalQty = totalQty.add(item.getQuantity());
                    totalAmount = totalAmount.add(item.getAmount());
                }
            }
            com.beefarm.common.utils.BusinessChecks.quantity(totalQty);
            com.beefarm.common.utils.BusinessChecks.money(totalAmount);
            p.setTotalQty(totalQty);
            p.setTotalAmount(totalAmount);
            itemMapper.deleteByPurchaseId(p.getId());
            for (PurchaseItem item : p.getItems()) {
                item.setId(null);
                item.setPurchaseId(p.getId());
                itemMapper.insert(item);
            }
        }
        baseMapper.updateById(p);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchase(Long id) {
        businessGuard.lock();
        Purchase p = baseMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("采购单不存在");
        }
        if (p.getStatus() != null && p.getStatus() == 1) {
            throw new BusinessException("已审核的采购单不能删除");
        }
        itemMapper.deleteByPurchaseId(id);
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id) {
        businessGuard.lock();
        Purchase p = baseMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("采购单不存在");
        }
        if (p.getStatus() == null || p.getStatus() != 0) {
            throw new BusinessException("该采购单已处理，请勿重复审核");
        }
        if (p.getWarehouseId() == null) {
            throw new BusinessException("采购单未指定入库仓库");
        }
        checkWarehouse(p.getWarehouseId());
        List<PurchaseItem> items = itemMapper.selectItemsByPurchaseId(id);
        com.beefarm.common.utils.BusinessChecks.require(!items.isEmpty(), "采购单缺少明细");
        for (PurchaseItem item : items) {
            com.beefarm.common.utils.BusinessChecks.quantity(item.getQuantity());
            item.setSpec(item.getSpec() == null ? "" : item.getSpec().trim());
            // 蜂具耗材(item_type=1)按 物料名+规格+入库仓库 查找或新建台账
            Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getItemType, 1)
                    .eq(Inventory::getWarehouseId, p.getWarehouseId())
                    .eq(Inventory::getItemName, item.getMaterialName())
                    .and(w -> w.eq(Inventory::getSpec, item.getSpec()).or(item.getSpec().isEmpty()).isNull(item.getSpec().isEmpty(), Inventory::getSpec)));
            if (inventory == null) {
                inventory = new Inventory();
                inventory.setItemType(1);
                inventory.setWarehouseId(p.getWarehouseId());
                inventory.setItemName(item.getMaterialName());
                inventory.setSpec(item.getSpec());
                inventory.setUnit(item.getUnit());
                inventory.setStock(BigDecimal.ZERO);
                inventory.setWarnStock(BigDecimal.TEN);
                inventoryMapper.insert(inventory);
            }
            BigDecimal qty = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
            BigDecimal after = inventory.getStock().add(qty);
            com.beefarm.common.utils.BusinessChecks.money(after);
            inventory.setStock(after);
            inventoryMapper.updateById(inventory);

            StockRecord record = new StockRecord();
            record.setItemId(inventory.getId());
            record.setWarehouseId(p.getWarehouseId());
            record.setBizType(1);
            record.setQuantity(qty);
            record.setStockAfter(after);
            record.setOperator(com.beefarm.common.utils.SecurityUtils.getUsername());
            record.setRemark("采购入库 " + p.getPurchaseNo());
            stockRecordMapper.insert(record);
        }
        p.setStatus(1);
        baseMapper.updateById(p);
    }
}
