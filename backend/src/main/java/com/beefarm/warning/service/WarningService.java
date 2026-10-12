package com.beefarm.warning.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.warning.entity.Warning;

/**
 * 预警中心
 */
public interface WarningService extends IService<Warning> {

    /** 分页查询 */
    IPage<Warning> pageList(Integer pageNum, Integer pageSize,
                            String warningType, Integer status, Integer level);

    /** 处理预警 */
    void handle(Long id, String remark);

    /** 按实时规则补录未处理预警，返回新增条数 */
    int regenerate();
}
