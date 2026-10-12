package com.beefarm.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.finance.entity.Expense;
import com.beefarm.finance.mapper.ExpenseMapper;
import com.beefarm.finance.service.ExpenseService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ExpenseServiceImpl extends ServiceImpl<ExpenseMapper, Expense> implements ExpenseService {

    @Override
    public IPage<Expense> pageList(Integer pageNum, Integer pageSize, Expense query,
                                   LocalDate beginTime, LocalDate endTime) {
        return baseMapper.selectExpensePage(new Page<>(pageNum, pageSize), query, beginTime, endTime);
    }
}
