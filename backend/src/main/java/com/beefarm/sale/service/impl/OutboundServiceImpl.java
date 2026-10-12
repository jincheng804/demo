package com.beefarm.sale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.exception.BusinessException;
import com.beefarm.produce.entity.ProductBatch;
import com.beefarm.produce.mapper.ProductBatchMapper;
import com.beefarm.sale.entity.Outbound;
import com.beefarm.sale.entity.OutboundItem;
import com.beefarm.sale.mapper.OutboundItemMapper;
import com.beefarm.sale.mapper.OutboundMapper;
import com.beefarm.sale.service.OutboundService;
import com.beefarm.stock.entity.Inventory;
import com.beefarm.stock.entity.StockRecord;
import com.beefarm.stock.entity.Warehouse;
import com.beefarm.stock.mapper.InventoryMapper;
import com.beefarm.stock.mapper.StockRecordMapper;
import com.beefarm.stock.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundServiceImpl extends ServiceImpl<OutboundMapper, Outbound>
        implements OutboundService {
    private final com.beefarm.common.service.BusinessGuard businessGuard;

    private final OutboundItemMapper itemMapper;
    private final ProductBatchMapper productBatchMapper;
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
    public IPage<Outbound> pageList(Integer pageNum, Integer pageSize, Outbound query) {
        return baseMapper.selectOutboundPage(new Page<>(pageNum, pageSize), query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOutbound(Outbound o) {
        businessGuard.lock();
        com.beefarm.common.utils.BusinessChecks.require(o.getId() == null && o.getCustomerId() != null, "请填写客户且不要指定单据ID");
        if (o.getItems() == null || o.getItems().isEmpty()) {
            throw new BusinessException("请添加出库明细");
        }
        if (o.getOutboundDate() == null) {
            o.setOutboundDate(LocalDate.now());
        }
        // 出库单号 CK + yyyyMMdd + 4位流水
        String no = businessGuard.nextNumber("CK", o.getOutboundDate());
        o.setOutboundNo(no);
        o.setStatus(0);

        // 汇总
        int totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OutboundItem item : o.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("出库数量必须大于0");
            }
            com.beefarm.common.utils.BusinessChecks.require(item.getProductBatchId() != null, "请选择产品批次");
            ProductBatch product = productBatchMapper.selectById(item.getProductBatchId());
            com.beefarm.common.utils.BusinessChecks.require(product != null && Integer.valueOf(1).equals(product.getQcStatus()), "只能销售质检合格的产品批次");
            com.beefarm.common.utils.BusinessChecks.money(item.getPrice());
            item.setId(null);
            totalQty = Math.addExact(totalQty, item.getQuantity());
            totalAmount = totalAmount.add(
                    item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        o.setTotalQty(totalQty);
        com.beefarm.common.utils.BusinessChecks.money(totalAmount);
        o.setTotalAmount(totalAmount);
        baseMapper.insert(o);

        for (OutboundItem item : o.getItems()) {
            item.setOutboundId(o.getId());
            itemMapper.insert(item);
        }
        return no;
    }

    @Override
    public Outbound detail(Long id) {
        Outbound o = baseMapper.selectById(id);
        if (o == null) {
            throw new BusinessException("出库单不存在");
        }
        o.setItems(itemMapper.selectItemsByOutboundId(id));
        return o;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id) {
        businessGuard.lock();
        Outbound o = baseMapper.selectById(id);
        if (o == null) {
            throw new BusinessException("出库单不存在");
        }
        com.beefarm.common.utils.BusinessChecks.require(
                !java.util.Objects.equals(o.getCreateBy(), com.beefarm.common.utils.SecurityUtils.getUsername()),
                "不能审核本人创建的出库单，请由其他有审核权限的人员处理");
        if (!Integer.valueOf(0).equals(o.getStatus())) {
            throw new BusinessException("该出库单已处理，请勿重复审核");
        }
        List<OutboundItem> items = itemMapper.selectItemsByOutboundId(id);
        Long whId = finishedWarehouseId();
        for (OutboundItem item : items) {
            ProductBatch batch = productBatchMapper.selectById(item.getProductBatchId());
            if (batch == null) {
                throw new BusinessException("产品批次不存在");
            }
            com.beefarm.common.utils.BusinessChecks.require(Integer.valueOf(1).equals(batch.getQcStatus()), "该产品尚未质检合格，不能出库");
            // Batch availability is independent of the shared name/spec inventory ledger.
            BigDecimal batchBalance = stockRecordMapper.availableBatch(batch.getId());
            com.beefarm.common.utils.BusinessChecks.require(batchBalance.compareTo(BigDecimal.valueOf(item.getQuantity())) >= 0, "该产品批次可用数量不足");
            // 查找对应产品库存台账（成品库口径）
            Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getItemType, 2)
                    .eq(Inventory::getWarehouseId, whId)
                    .eq(Inventory::getItemName, batch.getProductName())
                    .eq(Inventory::getSpec, batch.getSpec()));
            if (inventory == null) {
                throw new BusinessException("产品【" + batch.getProductName() + "】无库存台账");
            }
            BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
            if (inventory.getStock().compareTo(qty) < 0) {
                throw new BusinessException("产品【" + batch.getProductName() + " " + batch.getSpec()
                        + "】库存不足，当前 " + inventory.getStock());
            }
            BigDecimal after = inventory.getStock().subtract(qty);
            inventory.setStock(after);
            inventoryMapper.updateById(inventory);

            StockRecord record = new StockRecord();
            record.setItemId(inventory.getId());
            record.setBatchId(batch.getId());
            record.setWarehouseId(whId);
            record.setBizType(2);
            record.setQuantity(qty.negate());
            record.setStockAfter(after);
            record.setRemark("销售出库" + o.getOutboundNo());
            record.setOperator(com.beefarm.common.utils.SecurityUtils.getUsername());
            stockRecordMapper.insert(record);
        }
        o.setStatus(1);
        baseMapper.updateById(o);
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void deleteDraft(Long id) {
        businessGuard.lock();
        Outbound order = baseMapper.selectById(id);
        com.beefarm.common.utils.BusinessChecks.require(order != null, "出库单不存在");
        com.beefarm.common.utils.BusinessChecks.require(Integer.valueOf(0).equals(order.getStatus()), "已审核出库单不能删除");
        itemMapper.deleteByOutboundId(id);
        baseMapper.deleteById(id);
    }
}
