package com.beefarm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beefarm.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /** 根据用户ID查询其可见菜单(目录/菜单)，用于构建路由 */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /** 查询全部菜单(目录/菜单) */
    List<SysMenu> selectAllMenus();
}
