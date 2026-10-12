package com.beefarm.disease.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.disease.entity.Disease;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DiseaseMapper extends BaseMapper<Disease> {
    IPage<Disease> selectDiseasePage(IPage<Disease> page, @Param("disease") Disease disease);
}
