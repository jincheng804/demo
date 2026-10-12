package com.beefarm.sale.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.sale.entity.Outbound;
import com.beefarm.sale.service.OutboundService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "出库管理")
@RestController
@RequestMapping("/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @PreAuthorize("@ss.hasPermi('outbound:list')")
    @GetMapping("/list")
    public TableDataInfo<Outbound> list(Outbound query,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(outboundService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('outbound:list')")
    @GetMapping("/{id}")
    public R<Outbound> detail(@PathVariable Long id) {
        return R.ok(outboundService.detail(id));
    }

    @PreAuthorize("@ss.hasPermi('outbound:add')")
    @PostMapping
    public R<String> add(@RequestBody Outbound outbound) {
        return R.ok("出库单创建成功", outboundService.createOutbound(outbound));
    }
    @PreAuthorize("@ss.hasPermi('outbound:audit')")
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id) {
        outboundService.audit(id);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('outbound:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        outboundService.deleteDraft(id);
        return R.ok();
    }
}
