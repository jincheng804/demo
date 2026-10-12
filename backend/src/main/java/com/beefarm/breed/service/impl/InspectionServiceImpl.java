package com.beefarm.breed.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.breed.entity.Inspection;
import com.beefarm.breed.mapper.InspectionMapper;
import com.beefarm.breed.service.InspectionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class InspectionServiceImpl extends ServiceImpl<InspectionMapper, Inspection>
        implements InspectionService {

    @Override
    public IPage<Inspection> pageList(Integer pageNum, Integer pageSize, Inspection query,
                                     LocalDate beginTime, LocalDate endTime) {
        return baseMapper.selectInspectionPage(new Page<>(pageNum, pageSize), query, beginTime, endTime);
    }
}
