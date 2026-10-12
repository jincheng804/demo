package com.beefarm.stock.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.stock.entity.Inventory;
import com.beefarm.stock.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "库存台账")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PreAuthorize("@ss.hasPermi('inventory:list')")
    @GetMapping("/list")
    public TableDataInfo<Inventory> list(Inventory query,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(inventoryService.page(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Inventory>()
                        .like(org.springframework.util.StringUtils.hasText(query.getItemName()), Inventory::getItemName, query.getItemName())
                        .eq(query.getItemType() != null, Inventory::getItemType, query.getItemType())
                        .eq(query.getWarehouseId() != null, Inventory::getWarehouseId, query.getWarehouseId())
                        .orderByDesc(Inventory::getId)));
    }

    @PreAuthorize("@ss.hasPermi('inventory:add')")
    @PostMapping
    public R<Void> add(@RequestBody Inventory inventory) {
        inventoryService.createItem(inventory);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('inventory:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Inventory inventory) {
        inventoryService.updateItem(inventory);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('inventory:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return R.ok();
    }

    /** 出入库操作 */
    @PreAuthorize("@ss.hasPermi('inventory:stock')")
    @PostMapping("/op")
    public R<Void> stockOp(@RequestParam Long itemId,
                           @RequestParam Integer bizType,
                           @RequestParam BigDecimal quantity,
                           @RequestParam(required = false) String remark) {
        inventoryService.stockOp(itemId, bizType, quantity, remark);
        return R.ok();
    }
}
