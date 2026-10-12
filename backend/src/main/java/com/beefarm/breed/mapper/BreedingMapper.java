package com.beefarm.breed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.breed.entity.Breeding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface BreedingMapper extends BaseMapper<Breeding> {

    /** 分页 + 多条件 + 日期范围 + 关联蜂箱编号(源/目标) */
    IPage<Breeding> selectBreedingPage(IPage<Breeding> page,
                                     @Param("query") Breeding query,
                                     @Param("beginTime") LocalDate beginTime,
                                     @Param("endTime") LocalDate endTime);
}
