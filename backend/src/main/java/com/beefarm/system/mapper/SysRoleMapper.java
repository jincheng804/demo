package com.beefarm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beefarm.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    @org.apache.ibatis.annotations.Select("SELECT DISTINCT m.perms FROM sys_role_menu rm JOIN sys_menu m ON m.id=rm.menu_id WHERE rm.role_id=#{id} AND m.perms IS NOT NULL AND m.perms != '' AND m.status=1")
    java.util.List<String> selectPermissions(@Param("id") Long id);


    /** 查询角色关联的菜单ID */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /** 删除角色的菜单关联 */
    int deleteRoleMenu(@Param("roleId") Long roleId);

    /** 批量新增角色菜单 */
    int batchRoleMenu(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
