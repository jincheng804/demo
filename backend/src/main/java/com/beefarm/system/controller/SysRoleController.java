package com.beefarm.system.controller;

import com.beefarm.common.core.R;
import com.beefarm.system.entity.SysRole;
import com.beefarm.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @PreAuthorize("@ss.hasPermi('role:list')")
    @GetMapping("/list")
    public R<List<SysRole>> list() {
        return R.ok(roleService.list());
    }

    @PreAuthorize("@ss.hasAnyPermi('role:list,user:list,user:add,user:edit')")
    @GetMapping("/all")
    public R<List<SysRole>> all() {
        return R.ok(roleService.listAll());
    }

    @PreAuthorize("@ss.hasPermi('role:list')")
    @GetMapping("/{id}")
    public R<SysRole> detail(@PathVariable Long id) {
        return R.ok(roleService.detail(id));
    }

    @PreAuthorize("@ss.hasPermi('role:add') and @ss.hasRole('admin')")
    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        roleService.saveRole(role);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('role:edit') and @ss.hasRole('admin')")
    @PutMapping
    public R<Void> edit(@RequestBody SysRole role) {
        roleService.saveRole(role);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('role:remove') and @ss.hasRole('admin')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        var role = roleService.getById(id);
        com.beefarm.common.utils.BusinessChecks.require(role != null && !"admin".equals(role.getRoleCode()), "管理员角色不能删除");
        roleService.removeById(id);
        return R.ok();
    }
}
