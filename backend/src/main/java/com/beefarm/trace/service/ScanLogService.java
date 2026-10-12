package com.beefarm.trace.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.trace.entity.ScanLog;

import java.time.LocalDate;

public interface ScanLogService extends IService<ScanLog> {

    /** 只读分页：溯源码 / 设备类型 / 日期范围 */
    IPage<ScanLog> pageList(Integer pageNum, Integer pageSize, ScanLog query,
                           LocalDate beginTime, LocalDate endTime);
}
