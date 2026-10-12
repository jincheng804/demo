package com.beefarm.breed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.breed.entity.Queen;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QueenMapper extends BaseMapper<Queen> {
    IPage<Queen> selectQueenPage(IPage<Queen> page, @Param("queen") Queen queen);
}
