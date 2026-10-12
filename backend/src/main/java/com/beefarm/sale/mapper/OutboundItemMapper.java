package com.beefarm.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beefarm.sale.entity.OutboundItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OutboundItemMapper extends BaseMapper<OutboundItem> {

    /** 查询出库单明细(含产品信息) */
    List<OutboundItem> selectItemsByOutboundId(@Param("outboundId") Long outboundId);

    int deleteByOutboundId(@Param("outboundId") Long outboundId);
}
