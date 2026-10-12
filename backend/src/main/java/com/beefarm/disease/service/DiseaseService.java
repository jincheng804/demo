package com.beefarm.disease.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.disease.entity.Disease;

public interface DiseaseService extends IService<Disease> {
    IPage<Disease> pageList(Integer pageNum, Integer pageSize, Disease query);
}
