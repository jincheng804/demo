package com.beefarm.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.finance.entity.Expense;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface ExpenseMapper extends BaseMapper<Expense> {

    /** 分页查询(含蜂场名称、日期范围) */
    IPage<Expense> selectExpensePage(IPage<Expense> page, @Param("expense") Expense expense,
                                     @Param("beginTime") LocalDate beginTime,
                                     @Param("endTime") LocalDate endTime);
}
