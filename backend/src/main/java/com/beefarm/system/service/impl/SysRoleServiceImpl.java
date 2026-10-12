package com.beefarm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.exception.BusinessException;
import com.beefarm.system.entity.SysRole;
import com.beefarm.system.mapper.SysRoleMapper;
import com.beefarm.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

    @Override
    public List<SysRole> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getOrderNum));
    }

    @Override
    public SysRole detail(Long id) {
        SysRole role = baseMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        role.setMenuIds(baseMapper.selectMenuIdsByRoleId(id));
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(SysRole role) {
        if (role.getId() != null) {
            SysRole original = baseMapper.selectById(role.getId());
            com.beefarm.common.utils.BusinessChecks.require(original != null, "角色不存在");
            if ("admin".equals(original.getRoleCode())) {
                com.beefarm.common.utils.BusinessChecks.require(Integer.valueOf(1).equals(role.getStatus()), "管理员角色不能停用");
                role.setRoleCode("admin");
            }
        }
        if (role.getId() == null) {
            long exists = this.count(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleCode, role.getRoleCode()));
            if (exists > 0) {
                throw new BusinessException("角色编码已存在");
            }
            baseMapper.insert(role);
        } else {
            baseMapper.updateById(role);
        }
        // 重建角色-菜单
        if (role.getMenuIds() != null) {
            baseMapper.deleteRoleMenu(role.getId());
            if (!role.getMenuIds().isEmpty()) {
                baseMapper.batchRoleMenu(role.getId(), role.getMenuIds());
            }
        }
    }
}
