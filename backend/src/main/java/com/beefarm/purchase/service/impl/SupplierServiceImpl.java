package com.beefarm.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.purchase.entity.Supplier;
import com.beefarm.purchase.mapper.SupplierMapper;
import com.beefarm.purchase.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier>
        implements SupplierService {

    @Override
    public IPage<Supplier> pageList(Integer pageNum, Integer pageSize, Supplier query,
                                   LocalDate beginTime, LocalDate endTime) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getSupplierName()),
                    Supplier::getSupplierName, query.getSupplierName());
            wrapper.eq(StringUtils.hasText(query.getSupplierType()),
                    Supplier::getSupplierType, query.getSupplierType());
            wrapper.like(StringUtils.hasText(query.getContact()),
                    Supplier::getContact, query.getContact());
            wrapper.eq(query.getStatus() != null,
                    Supplier::getStatus, query.getStatus());
        }
        wrapper.ge(beginTime != null, Supplier::getCreateTime, beginTime);
        wrapper.le(endTime != null, Supplier::getCreateTime,
                endTime == null ? null : endTime.atTime(23, 59, 59));
        wrapper.orderByDesc(Supplier::getId);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
