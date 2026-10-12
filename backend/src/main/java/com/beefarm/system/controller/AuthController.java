package com.beefarm.system.controller;

import com.beefarm.common.core.R;
import com.beefarm.common.utils.SecurityUtils;
import com.beefarm.security.JwtTokenProvider;
import com.beefarm.security.LoginUser;
import com.beefarm.system.dto.LoginDTO;
import com.beefarm.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SysMenuService menuService;
    private final com.beefarm.system.mapper.SysUserMapper userMapper;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = tokenProvider.createToken(loginUser);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        log.info("用户[{}]登录成功 IP={}", dto.getUsername(), request.getRemoteAddr());
        return R.ok("登录成功", result);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public R<Map<String, Object>> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Map<String, Object> info = new HashMap<>();
        info.put("user", buildUserInfo(loginUser));
        info.put("roles", loginUser.getRoles());
        info.put("permissions", loginUser.getPermissions());
        return R.ok(info);
    }

    @Operation(summary = "获取动态路由")
    @GetMapping("/routers")
    public R<List<Map<String, Object>>> getRouters() {
        return R.ok(menuService.buildRouters());
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        userMapper.invalidateTokens(SecurityUtils.getUserId());
        return R.ok();
    }

    private Map<String, Object> buildUserInfo(LoginUser u) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", u.getUserId());
        user.put("deptId", u.getDeptId());
        user.put("username", u.getUsername());
        user.put("realName", u.getRealName());
        user.put("avatar", u.getAvatar());
        return user;
    }
}
