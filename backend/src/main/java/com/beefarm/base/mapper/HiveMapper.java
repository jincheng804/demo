package com.beefarm.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.base.entity.Hive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HiveMapper extends BaseMapper<Hive> {

    /** 分页查询(含分区名称) */
    IPage<Hive> selectHivePage(IPage<Hive> page, @Param("hive") Hive hive);
}
