package com.beefarm.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.base.entity.NectarSource;
import com.beefarm.base.mapper.NectarSourceMapper;
import com.beefarm.base.service.NectarSourceService;
import org.springframework.stereotype.Service;

@Service
public class NectarSourceServiceImpl extends ServiceImpl<NectarSourceMapper, NectarSource>
        implements NectarSourceService {
}
