package com.beefarm.warning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beefarm.warning.entity.Warning;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WarningMapper extends BaseMapper<Warning> {
}
