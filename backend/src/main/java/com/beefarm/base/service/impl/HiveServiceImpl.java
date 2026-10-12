package com.beefarm.base.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.base.entity.Hive;
import com.beefarm.base.mapper.HiveMapper;
import com.beefarm.base.service.HiveService;
import org.springframework.stereotype.Service;

@Service
public class HiveServiceImpl extends ServiceImpl<HiveMapper, Hive> implements HiveService {

    @Override
    public IPage<Hive> pageList(Integer pageNum, Integer pageSize, Hive query) {
        Page<Hive> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectHivePage(page, query);
    }
}
