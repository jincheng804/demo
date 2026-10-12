package com.beefarm.produce.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.produce.entity.ProductBatch;
import com.beefarm.produce.service.ProductBatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "产品批次")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductBatchController {

    private final ProductBatchService productBatchService;

    @PreAuthorize("@ss.hasPermi('product:list')")
    @GetMapping("/list")
    public TableDataInfo<ProductBatch> list(ProductBatch query,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(productBatchService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('product:add')")
    @PostMapping
    public R<String> add(@RequestBody ProductBatch product) {
        return R.ok("装瓶成功，已自动入库", productBatchService.bottle(product));
    }

    @PreAuthorize("@ss.hasPermi('product:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody ProductBatch product) {
        com.beefarm.common.utils.BusinessChecks.require(product.getId() != null && productBatchService.getById(product.getId()) != null, "产品批次不存在");
        com.beefarm.common.utils.BusinessChecks.money(product.getPrice());
        ProductBatch update = new ProductBatch();
        update.setId(product.getId());
        update.setPrice(product.getPrice());
        productBatchService.updateById(update);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('product:edit')")
    @PutMapping("/price")
    public R<Void> updatePrice(@RequestParam Long id, @RequestParam java.math.BigDecimal price) {
        com.beefarm.common.utils.BusinessChecks.money(price);
        com.beefarm.common.utils.BusinessChecks.require(productBatchService.getById(id) != null, "产品批次不存在");
        ProductBatch update = new ProductBatch();
        update.setId(id);
        update.setPrice(price);
        productBatchService.updateById(update);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('product:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        productBatchService.removeBatch(id);
        return R.ok();
    }
}
