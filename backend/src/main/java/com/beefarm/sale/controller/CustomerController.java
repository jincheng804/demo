package com.beefarm.sale.controller;

import com.beefarm.common.core.R;
import com.beefarm.sale.entity.Customer;
import com.beefarm.sale.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户管理")
@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PreAuthorize("@ss.hasPermi('customer:list')")
    @GetMapping("/list")
    public R<List<Customer>> list() {
        return R.ok(customerService.list());
    }

    @PreAuthorize("@ss.hasPermi('customer:add')")
    @PostMapping
    public R<Void> add(@RequestBody Customer customer) {
        customerService.save(customer);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('customer:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Customer customer) {
        customerService.updateById(customer);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('customer:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        customerService.removeById(id);
        return R.ok();
    }
}
