package com.beefarm.common.core;

import lombok.Data;

/**
 * 分页查询基础参数
 */
@Data
public class PageQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    /** 排序字段 */
    private String orderByColumn;
    /** asc/desc */
    private String isAsc;
}
