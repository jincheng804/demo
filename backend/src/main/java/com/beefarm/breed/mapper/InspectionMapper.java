package com.beefarm.breed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.breed.entity.Inspection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface InspectionMapper extends BaseMapper<Inspection> {

    /** 分页 + 多条件 + 日期范围 + 关联蜂箱编号 */
    IPage<Inspection> selectInspectionPage(IPage<Inspection> page,
                                          @Param("query") Inspection query,
                                          @Param("beginTime") LocalDate beginTime,
                                          @Param("endTime") LocalDate endTime);
}
