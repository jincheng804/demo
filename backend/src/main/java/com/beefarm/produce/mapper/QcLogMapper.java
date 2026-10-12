package com.beefarm.produce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beefarm.produce.entity.QcLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QcLogMapper extends BaseMapper<QcLog> {
}
