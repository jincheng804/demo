package com.beefarm.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.purchase.entity.Purchase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface PurchaseMapper extends BaseMapper<Purchase> {

    /** 分页 + 多条件 + 日期范围 + 关联供应商/仓库名称 */
    IPage<Purchase> selectPurchasePage(IPage<Purchase> page,
                                     @Param("query") Purchase query,
                                     @Param("beginTime") LocalDate beginTime,
                                     @Param("endTime") LocalDate endTime);
}
