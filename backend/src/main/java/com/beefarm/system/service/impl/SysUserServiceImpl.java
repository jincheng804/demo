package com.beefarm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.exception.BusinessException;
import com.beefarm.system.entity.SysDept;
import com.beefarm.system.entity.SysUser;
import com.beefarm.system.mapper.SysDeptMapper;
import com.beefarm.system.mapper.SysUserMapper;
import com.beefarm.system.mapper.SysUserRoleMapper;
import com.beefarm.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;
    private final com.beefarm.system.mapper.SysRoleMapper roleMapper;

    @Override
    public IPage<SysUser> pageList(Integer pageNum, Integer pageSize, SysUser query) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> result = baseMapper.selectPage(page,
                new LambdaQueryWrapper<SysUser>()
                        .like(query.getUsername() != null, SysUser::getUsername,
                                query.getUsername() == null ? null : query.getUsername())
                        .like(query.getRealName() != null, SysUser::getRealName,
                                query.getRealName() == null ? null : query.getRealName())
                        .eq(query.getPhone() != null, SysUser::getPhone,
                                query.getPhone() == null ? null : query.getPhone())
                        .eq(query.getDeptId() != null, SysUser::getDeptId,
                                query.getDeptId() == null ? null : query.getDeptId())
                        .orderByDesc(SysUser::getId));
        result.getRecords().forEach(this::fillExtra);
        return result;
    }

    private void fillExtra(SysUser user) {
        user.setRoleIds(baseMapper.selectRoleIdsByUserId(user.getId()));
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            user.setDeptName(dept == null ? null : dept.getDeptName());
        }
        user.setPassword(null);
    }

    @Override
    public SysUser detail(Long id) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        fillExtra(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUser user) {
        com.beefarm.common.utils.BusinessChecks.require(user.getId() == null, "新增用户不能指定ID");
        com.beefarm.common.utils.BusinessChecks.require(user.getUsername() != null && !user.getUsername().isBlank(), "请输入登录账号");
        checkRoles(user);
        user.setAuthVersion(0);
        long exists = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername()));
        if (exists > 0) {
            throw new BusinessException("登录账号已存在");
        }
        String pwd = (user.getPassword() == null || user.getPassword().isEmpty())
                ? "123456" : user.getPassword();
        validatePassword(pwd);
        user.setPassword(passwordEncoder.encode(pwd));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        baseMapper.insert(user);
        saveRoles(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        protectUser(user.getId());
        checkRoles(user);
        user.setAuthVersion(null);
        user.setUsername(null);
        user.setPassword(null);
        baseMapper.updateById(user);
        if (user.getRoleIds() != null) {
            userRoleMapper.deleteByUserId(user.getId());
            saveRoles(user);
        }
    }

    private void saveRoles(SysUser user) {
        if (!CollectionUtils.isEmpty(user.getRoleIds())) {
            userRoleMapper.batchInsert(user.getId(), user.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String password) {
        protectUser(id);
        String pwd = (password == null || password.isEmpty()) ? "123456" : password;
        SysUser update = new SysUser();
        update.setId(id);
        validatePassword(pwd);
        update.setPassword(passwordEncoder.encode(pwd));
        baseMapper.updateById(update);
        baseMapper.invalidateTokens(id);
    }

    private void validatePassword(String password) {
        com.beefarm.common.utils.BusinessChecks.require(password != null && password.length() >= 6
                && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72, "密码至少6位且不超过72字节");
    }
    private void protectUser(Long id) {
        com.beefarm.common.utils.BusinessChecks.require(id != null && baseMapper.selectById(id) != null, "用户不存在");
        com.beefarm.common.utils.BusinessChecks.require(!baseMapper.selectRoleCodesByUserId(id).contains("admin")
                || com.beefarm.common.utils.SecurityUtils.isAdmin(), "只有管理员可以维护管理员账号");
    }
    private void checkRoles(SysUser user) {
        if (user.getRoleIds() == null) return;
        var current = com.beefarm.common.utils.SecurityUtils.getLoginUser();
        for (Long id : user.getRoleIds()) {
            var role = roleMapper.selectById(id);
            com.beefarm.common.utils.BusinessChecks.require(role != null && Integer.valueOf(1).equals(role.getStatus()), "角色不存在或已停用");
            if (!com.beefarm.common.utils.SecurityUtils.isAdmin()) {
                com.beefarm.common.utils.BusinessChecks.require(!"admin".equals(role.getRoleCode()), "不能授予管理员角色");
                com.beefarm.common.utils.BusinessChecks.require(current.getPermissions().containsAll(roleMapper.selectPermissions(id)), "不能授予超出本人权限的角色");
            }
        }
        user.setRoleIds(user.getRoleIds().stream().distinct().toList());
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        protectUser(id);
        com.beefarm.common.utils.BusinessChecks.require(!id.equals(com.beefarm.common.utils.SecurityUtils.getUserId()), "不能删除当前登录账号");
        com.beefarm.common.utils.BusinessChecks.require(!baseMapper.selectRoleCodesByUserId(id).contains("admin"), "管理员账号不可删除");
        userRoleMapper.deleteByUserId(id);
        baseMapper.deleteById(id);
    }

}
