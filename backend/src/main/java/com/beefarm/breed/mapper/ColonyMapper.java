package com.beefarm.breed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.breed.entity.Colony;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ColonyMapper extends BaseMapper<Colony> {
    IPage<Colony> selectColonyPage(IPage<Colony> page, @Param("colony") Colony colony);
}
