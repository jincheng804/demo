package com.beefarm.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.sale.entity.Outbound;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OutboundMapper extends BaseMapper<Outbound> {
    IPage<Outbound> selectOutboundPage(IPage<Outbound> page, @Param("outbound") Outbound outbound);
}
