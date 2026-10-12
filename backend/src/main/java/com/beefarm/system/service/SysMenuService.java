package com.beefarm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.system.entity.SysMenu;

import java.util.List;
import java.util.Map;

public interface SysMenuService extends IService<SysMenu> {

    /** 查询当前用户的菜单(平铺) */
    List<SysMenu> selectMenusByCurrentUser();

    /** 构建当前用户的前端路由树 */
    List<Map<String, Object>> buildRouters();

    /** 构建菜单树(管理用) */
    List<SysMenu> buildMenuTree(List<SysMenu> menus);

    /** 新增/修改菜单 */
    void saveMenu(SysMenu menu);
}
