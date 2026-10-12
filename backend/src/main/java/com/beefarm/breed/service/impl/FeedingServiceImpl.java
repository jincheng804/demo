package com.beefarm.breed.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.breed.entity.Feeding;
import com.beefarm.breed.mapper.FeedingMapper;
import com.beefarm.breed.service.FeedingService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class FeedingServiceImpl extends ServiceImpl<FeedingMapper, Feeding>
        implements FeedingService {

    @Override
    public IPage<Feeding> pageList(Integer pageNum, Integer pageSize, Feeding query,
                                  LocalDate beginTime, LocalDate endTime) {
        return baseMapper.selectFeedingPage(new Page<>(pageNum, pageSize), query, beginTime, endTime);
    }
}
