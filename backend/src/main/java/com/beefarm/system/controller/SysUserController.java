package com.beefarm.system.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.core.TableDataInfo;
import com.beefarm.common.exception.BusinessException;
import com.beefarm.common.utils.SecurityUtils;
import com.beefarm.system.entity.SysUser;
import com.beefarm.system.service.SysUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final com.beefarm.system.mapper.SysUserMapper userMapper;

    @PreAuthorize("@ss.hasPermi('user:list')")
    @GetMapping("/list")
    public TableDataInfo<SysUser> list(SysUser query,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return TableDataInfo.build(userService.pageList(pageNum, pageSize, query));
    }

    @PreAuthorize("@ss.hasPermi('user:list')")
    @GetMapping("/{id}")
    public R<SysUser> detail(@PathVariable Long id) {
        return R.ok(userService.detail(id));
    }

    @PreAuthorize("@ss.hasPermi('user:add')")
    @PostMapping
    public R<Void> add(@RequestBody SysUser user) {
        userService.addUser(user);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('user:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody SysUser user) {
        userService.updateUser(user);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('user:remove')")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('user:reset')")
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.id(), request.password());
        return R.ok();
    }

    public record ResetPasswordRequest(Long id, String password) {}

    // ============ 个人中心（登录用户均可操作自己的信息） ============

    @GetMapping("/profile")
    public R<SysUser> profile() {
        Long userId = SecurityUtils.getUserId();
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return R.ok(user);
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody SysUser user) {
        Long userId = SecurityUtils.getUserId();
        SysUser update = new SysUser();
        update.setId(userId);
        update.setRealName(user.getRealName());
        update.setPhone(user.getPhone());
        update.setEmail(user.getEmail());
        update.setGender(user.getGender());
        update.setAvatar(user.getAvatar());
        userService.updateById(update);
        return R.ok();
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @PutMapping("/password")
    public R<Void> updatePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new BusinessException("请输入旧密码");
        }
        if (newPassword == null || newPassword.length() < 6 || newPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("新密码至少6位且不超过72字节");
        }
        Long userId = SecurityUtils.getUserId();
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(update);
        userMapper.invalidateTokens(userId);
        return R.ok();
    }
}
