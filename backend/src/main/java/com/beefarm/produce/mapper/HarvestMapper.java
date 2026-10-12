package com.beefarm.produce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.produce.entity.Harvest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HarvestMapper extends BaseMapper<Harvest> {
    IPage<Harvest> selectHarvestPage(IPage<Harvest> page, @Param("harvest") Harvest harvest);
}
