package com.beefarm.security;

import com.beefarm.common.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 权限校验服务，供 @PreAuthorize("@ss.hasPermi('xxx')") 使用
 */
@Service("ss")
public class PermissionService {

    /** 校验是否具备某权限 */
    public boolean hasPermi(String permission) {
        if (permission == null || permission.isEmpty()) {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Set<String> permissions = loginUser.getPermissions();
        if (permissions == null) {
            return false;
        }
        return permissions.contains("*:*:*") || permissions.contains(permission.trim());
    }

    /** 校验是否具备任一权限 */
    public boolean hasAnyPermi(String permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        for (String p : permissions.split(",")) {
            if (hasPermi(p.trim())) {
                return true;
            }
        }
        return false;
    }

    /** 校验角色 */
    public boolean hasRole(String role) {
        if (role == null || role.isEmpty()) {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser.getRoles().contains("admin") || loginUser.getRoles().contains(role.trim());
    }
}
