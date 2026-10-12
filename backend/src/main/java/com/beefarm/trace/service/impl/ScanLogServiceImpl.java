package com.beefarm.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.utils.IpRegionUtils;
import com.beefarm.trace.entity.ScanLog;
import com.beefarm.trace.mapper.ScanLogMapper;
import com.beefarm.trace.service.ScanLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
public class ScanLogServiceImpl extends ServiceImpl<ScanLogMapper, ScanLog>
        implements ScanLogService {

    @Override
    public IPage<ScanLog> pageList(Integer pageNum, Integer pageSize, ScanLog query,
                                  LocalDate beginTime, LocalDate endTime) {
        LambdaQueryWrapper<ScanLog> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getTraceCode()),
                    ScanLog::getTraceCode, query.getTraceCode());
            wrapper.eq(StringUtils.hasText(query.getDeviceType()),
                    ScanLog::getDeviceType, query.getDeviceType());
        }
        wrapper.ge(beginTime != null, ScanLog::getScanTime,
                beginTime == null ? null : beginTime.atStartOfDay());
        wrapper.le(endTime != null, ScanLog::getScanTime,
                endTime == null ? null : endTime.atTime(23, 59, 59));
        wrapper.orderByDesc(ScanLog::getId);
        IPage<ScanLog> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        // 实时反查IP归属地填充location（不落库）
        page.getRecords().forEach(log -> log.setLocation(IpRegionUtils.getRegion(log.getIp())));
        return page;
    }
}
