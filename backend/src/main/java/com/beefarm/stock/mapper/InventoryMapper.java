package com.beefarm.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beefarm.stock.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
}
