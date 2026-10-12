package com.beefarm.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.stock.entity.Warehouse;
import com.beefarm.stock.mapper.WarehouseMapper;
import com.beefarm.stock.service.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse>
        implements WarehouseService {

    @Override
    public IPage<Warehouse> pageList(Integer pageNum, Integer pageSize, Warehouse query,
                                     LocalDate beginTime, LocalDate endTime) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getWhName()),
                    Warehouse::getWhName, query.getWhName());
            wrapper.eq(StringUtils.hasText(query.getWhType()),
                    Warehouse::getWhType, query.getWhType());
            wrapper.eq(query.getStatus() != null,
                    Warehouse::getStatus, query.getStatus());
        }
        wrapper.ge(beginTime != null, Warehouse::getCreateTime, beginTime);
        wrapper.le(endTime != null, Warehouse::getCreateTime,
                endTime == null ? null : endTime.atTime(23, 59, 59));
        wrapper.orderByDesc(Warehouse::getId);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
