package com.beefarm.common.utils;

import com.beefarm.common.exception.BusinessException;
import com.beefarm.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具：获取当前登录用户
 */
public class SecurityUtils {

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new BusinessException(401, "未获取到登录信息");
    }

    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    public static boolean isAdmin() {
        LoginUser u = getLoginUser();
        return u.getRoles().contains("admin");
    }

    /** 自动填充场景下安全获取用户名，未登录返回空 */
    public static String currentUsernameOrEmpty() {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a != null && a.isAuthenticated() && a.getPrincipal() instanceof LoginUser lu) {
                return lu.getUsername();
            }
        } catch (Exception ignored) {
        }
        return "";
    }
}
