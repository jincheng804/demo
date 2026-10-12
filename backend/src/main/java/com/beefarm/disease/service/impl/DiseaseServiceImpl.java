package com.beefarm.disease.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.disease.entity.Disease;
import com.beefarm.disease.mapper.DiseaseMapper;
import com.beefarm.disease.service.DiseaseService;
import org.springframework.stereotype.Service;

@Service
public class DiseaseServiceImpl extends ServiceImpl<DiseaseMapper, Disease> implements DiseaseService {
    @Override
    public IPage<Disease> pageList(Integer pageNum, Integer pageSize, Disease query) {
        return baseMapper.selectDiseasePage(new Page<>(pageNum, pageSize), query);
    }
}
