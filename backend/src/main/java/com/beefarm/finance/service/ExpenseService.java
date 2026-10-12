package com.beefarm.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.finance.entity.Expense;

import java.time.LocalDate;

/**
 * 成本费用
 */
public interface ExpenseService extends IService<Expense> {

    /** 分页 + 条件 + 日期范围 */
    IPage<Expense> pageList(Integer pageNum, Integer pageSize, Expense query,
                            LocalDate beginTime, LocalDate endTime);
}
