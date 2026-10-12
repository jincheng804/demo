package com.beefarm.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.base.entity.Apiary;
import com.beefarm.base.mapper.ApiaryMapper;
import com.beefarm.base.service.ApiaryService;
import org.springframework.stereotype.Service;

@Service
public class ApiaryServiceImpl extends ServiceImpl<ApiaryMapper, Apiary> implements ApiaryService {
}
