package com.beefarm.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.beefarm.system.entity.SysUser;
import com.beefarm.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户认证服务：加载用户、角色、权限
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new UsernameNotFoundException("账号已被停用，请联系管理员");
        }

        List<String> roleList = userMapper.selectRoleCodesByUserId(user.getId());
        List<String> permList = userMapper.selectPermsByUserId(user.getId());

        Set<String> roles = new HashSet<>(roleList);
        Set<String> perms = new HashSet<>(permList);
        // admin 拥有所有权限标识
        if (roles.contains("admin")) {
            perms.add("*:*:*");
        }

        return new LoginUser(
                user.getId(),
                user.getDeptId(),
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getAvatar(),
                user.getStatus(),
                user.getAuthVersion() == null ? 0 : user.getAuthVersion(),
                roles,
                perms
        );
    }
}
