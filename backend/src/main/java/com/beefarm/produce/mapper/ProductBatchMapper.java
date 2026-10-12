package com.beefarm.produce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.produce.entity.ProductBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductBatchMapper extends BaseMapper<ProductBatch> {
    @org.apache.ibatis.annotations.Select("SELECT (SELECT COUNT(*) FROM biz_outbound_item WHERE product_batch_id=#{id}) + " +
            "(SELECT COUNT(*) FROM biz_trace_code WHERE product_batch_id=#{id}) + " +
            "(SELECT COUNT(*) FROM biz_qc_record WHERE batch_id=#{id} AND deleted=0)")
    long countDependencies(@Param("id") Long id);

    IPage<ProductBatch> selectProductPage(IPage<ProductBatch> page, @Param("product") ProductBatch product);
}
