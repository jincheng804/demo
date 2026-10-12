package com.beefarm.produce.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.produce.entity.QcLog;
import com.beefarm.produce.entity.QcRecord;

import java.util.List;

public interface QcRecordService extends IService<QcRecord> {
    IPage<QcRecord> pageList(Integer pageNum, Integer pageSize, QcRecord query);

    /** 录入质检结果，并回写产品批次质检状态 */
    void addQc(QcRecord qc);

    /** 修改质检记录，记录修改历史 */
    void updateQcWithLog(QcRecord qc);

    /** 获取质检记录修改历史 */
    List<QcLog> getQcLogs(Long qcId);
}
