package com.beefarm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    List<SysRole> listAll();

    SysRole detail(Long id);

    void saveRole(SysRole role);
}
