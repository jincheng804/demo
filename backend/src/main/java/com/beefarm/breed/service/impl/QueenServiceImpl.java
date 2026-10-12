package com.beefarm.breed.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.breed.entity.Queen;
import com.beefarm.breed.mapper.QueenMapper;
import com.beefarm.breed.service.QueenService;
import org.springframework.stereotype.Service;

@Service
public class QueenServiceImpl extends ServiceImpl<QueenMapper, Queen> implements QueenService {
    @Override
    public IPage<Queen> pageList(Integer pageNum, Integer pageSize, Queen query) {
        return baseMapper.selectQueenPage(new Page<>(pageNum, pageSize), query);
    }
}
