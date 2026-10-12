package com.beefarm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beefarm.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @org.apache.ibatis.annotations.Update("UPDATE sys_user SET auth_version = COALESCE(auth_version, 0) + 1 WHERE id = #{id}")
    int invalidateTokens(@Param("id") Long id);


    /** 查询用户角色编码集合 */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /** 查询用户权限标识集合 */
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /** 查询用户角色ID集合 */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
