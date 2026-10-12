package com.beefarm.breed.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.breed.entity.Breeding;
import com.beefarm.breed.mapper.BreedingMapper;
import com.beefarm.breed.service.BreedingService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BreedingServiceImpl extends ServiceImpl<BreedingMapper, Breeding>
        implements BreedingService {

    @Override
    public IPage<Breeding> pageList(Integer pageNum, Integer pageSize, Breeding query,
                                   LocalDate beginTime, LocalDate endTime) {
        return baseMapper.selectBreedingPage(new Page<>(pageNum, pageSize), query, beginTime, endTime);
    }
}
