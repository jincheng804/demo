package com.beefarm.produce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.produce.entity.QcRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QcRecordMapper extends BaseMapper<QcRecord> {
    IPage<QcRecord> selectQcPage(IPage<QcRecord> page, @Param("qc") QcRecord qc);
}
