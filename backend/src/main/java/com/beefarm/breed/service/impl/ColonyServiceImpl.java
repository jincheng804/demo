package com.beefarm.breed.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.breed.entity.Colony;
import com.beefarm.breed.mapper.ColonyMapper;
import com.beefarm.breed.service.ColonyService;
import org.springframework.stereotype.Service;

@Service
public class ColonyServiceImpl extends ServiceImpl<ColonyMapper, Colony> implements ColonyService {
    @Override
    public IPage<Colony> pageList(Integer pageNum, Integer pageSize, Colony query) {
        return baseMapper.selectColonyPage(new Page<>(pageNum, pageSize), query);
    }
}
