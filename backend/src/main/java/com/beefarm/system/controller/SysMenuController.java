package com.beefarm.system.controller;

import com.beefarm.common.core.R;
import com.beefarm.system.entity.SysMenu;
import com.beefarm.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @PreAuthorize("@ss.hasPermi('menu:list')")
    @GetMapping("/list")
    public R<List<SysMenu>> list() {
        List<SysMenu> all = menuService.list();
        return R.ok(menuService.buildMenuTree(all));
    }

    @PreAuthorize("@ss.hasPermi('menu:list')")
    @GetMapping("/{id}")
    public R<SysMenu> detail(@PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    @PreAuthorize("@ss.hasPermi('menu:add') and @ss.hasRole('admin')")
    @PostMapping
    public R<Void> add(@RequestBody SysMenu menu) {
        menuService.saveMenu(menu);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('menu:edit') and @ss.hasRole('admin')")
    @PutMapping
    public R<Void> edit(@RequestBody SysMenu menu) {
        menuService.saveMenu(menu);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('menu:remove') and @ss.hasRole('admin')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        menuService.removeById(id);
        return R.ok();
    }
}
