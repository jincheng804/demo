package com.beefarm.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beefarm.purchase.entity.PurchaseItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PurchaseItemMapper extends BaseMapper<PurchaseItem> {

    /** 查询采购单明细 */
    List<PurchaseItem> selectItemsByPurchaseId(@Param("purchaseId") Long purchaseId);

    int deleteByPurchaseId(@Param("purchaseId") Long purchaseId);
}
