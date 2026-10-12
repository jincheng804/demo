package com.beefarm.trace.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.trace.entity.TraceCode;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface TraceService extends IService<TraceCode> {

    IPage<TraceCode> pageList(Integer pageNum, Integer pageSize, TraceCode query);

    /** 为产品批次生成溯源码 */
    TraceCode generate(Long productBatchId);

    /** 生成二维码图片字节 */
    byte[] qrImage(String traceCode);

    /** 聚合溯源全链路(公开)；request 非空时记录扫码日志 */
    Map<String, Object> publicTrace(String traceCode, HttpServletRequest request);
}
