package com.beefarm.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户（Spring Security 主体）
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private Long userId;
    private Long deptId;
    private String username;
    private String password;
    private String realName;
    private String avatar;
    private Integer status;
    private Integer authVersion;
    /** 角色编码集合 */
    private Set<String> roles;
    /** 权限标识集合 */
    private Set<String> permissions;

    public LoginUser(Long userId, Long deptId, String username, String password, String realName,
                     String avatar, Integer status, Integer authVersion, Set<String> roles, Set<String> permissions) {
        this.userId = userId;
        this.deptId = deptId;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.avatar = avatar;
        this.status = status;
        this.authVersion = authVersion;
        this.roles = roles;
        this.permissions = permissions;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 以权限标识作为 GrantedAuthority，角色加 ROLE_ 前缀
        Set<GrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return status == null || status == 1;
    }
}
