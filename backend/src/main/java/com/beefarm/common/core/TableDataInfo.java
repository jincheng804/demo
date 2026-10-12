package com.beefarm.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class TableDataInfo<T> implements Serializable {
    private long total;
    private List<T> rows;
    private int code;
    private String msg;

    public static <T> TableDataInfo<T> build(IPage<T> page) {
        TableDataInfo<T> t = new TableDataInfo<>();
        t.setCode(200);
        t.setMsg("查询成功");
        t.setTotal(page.getTotal());
        t.setRows(page.getRecords());
        return t;
    }

    public static <T> TableDataInfo<T> build(List<T> rows, long total) {
        TableDataInfo<T> t = new TableDataInfo<>();
        t.setCode(200);
        t.setMsg("查询成功");
        t.setTotal(total);
        t.setRows(rows);
        return t;
    }
}
