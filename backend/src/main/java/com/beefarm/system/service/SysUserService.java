package com.beefarm.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    void deleteUser(Long id);

    IPage<SysUser> pageList(Integer pageNum, Integer pageSize, SysUser query);

    SysUser detail(Long id);

    void addUser(SysUser user);

    void updateUser(SysUser user);

    void resetPassword(Long id, String password);
}
