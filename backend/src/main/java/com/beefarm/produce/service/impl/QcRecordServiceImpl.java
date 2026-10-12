package com.beefarm.produce.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.utils.SecurityUtils;
import com.beefarm.produce.entity.ProductBatch;
import com.beefarm.produce.entity.QcLog;
import com.beefarm.produce.entity.QcRecord;
import com.beefarm.produce.mapper.ProductBatchMapper;
import com.beefarm.produce.mapper.QcLogMapper;
import com.beefarm.produce.mapper.QcRecordMapper;
import com.beefarm.produce.service.QcRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QcRecordServiceImpl extends ServiceImpl<QcRecordMapper, QcRecord>
        implements QcRecordService {

    private final ProductBatchMapper productBatchMapper;
    private final QcLogMapper qcLogMapper;
    private final com.beefarm.common.service.BusinessGuard businessGuard;

    @Override
    public IPage<QcRecord> pageList(Integer pageNum, Integer pageSize, QcRecord query) {
        return baseMapper.selectQcPage(new Page<>(pageNum, pageSize), query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addQc(QcRecord qc) {
        businessGuard.lock();
        com.beefarm.common.utils.BusinessChecks.require(qc.getId() == null && qc.getBatchId() != null, "请选择产品批次");
        com.beefarm.common.utils.BusinessChecks.require(productBatchMapper.selectById(qc.getBatchId()) != null, "产品批次不存在");
        com.beefarm.common.utils.BusinessChecks.require(baseMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QcRecord>().eq(QcRecord::getBatchId, qc.getBatchId())) == 0, "该批次已有质检，请修改原记录并保留历史");
        com.beefarm.common.utils.BusinessChecks.require(qc.getQcResult() != null && java.util.Set.of(1, 2).contains(qc.getQcResult()), "请选择有效质检结果");
        if (qc.getQcDate() == null) {
            qc.setQcDate(LocalDate.now());
        }
        if (qc.getQcResult() == null) {
            qc.setQcResult(1);
        }
        baseMapper.insert(qc);
        // 回写产品批次质检状态
        ProductBatch batch = productBatchMapper.selectById(qc.getBatchId());
        if (batch != null) {
            batch.setQcStatus(qc.getQcResult());
            productBatchMapper.updateById(batch);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQcWithLog(QcRecord qc) {
        businessGuard.lock();
        QcRecord old = baseMapper.selectById(qc.getId());
        com.beefarm.common.utils.BusinessChecks.require(old != null, "质检记录不存在");
        com.beefarm.common.utils.BusinessChecks.require(qc.getBatchId() == null || qc.getBatchId().equals(old.getBatchId()), "不能更换质检所属批次");
        qc.setBatchId(old.getBatchId());
        com.beefarm.common.utils.BusinessChecks.require(qc.getQcResult() != null && java.util.Set.of(1, 2).contains(qc.getQcResult()), "请选择有效质检结果");
        String operator = SecurityUtils.getLoginUser().getRealName();
        List<QcLog> logs = new ArrayList<>();
        // 对比字段
        compareField(logs, qc.getId(), "qcResult", "质检结果",
                old.getQcResult() == null ? null : String.valueOf(old.getQcResult()),
                qc.getQcResult() == null ? null : String.valueOf(qc.getQcResult()), operator);
        compareField(logs, qc.getId(), "qcItems", "检测项目", old.getQcItems(), qc.getQcItems(), operator);
        compareField(logs, qc.getId(), "qcValue", "检测数值", old.getQcValue(), qc.getQcValue(), operator);
        compareField(logs, qc.getId(), "inspector", "检测人", old.getInspector(), qc.getInspector(), operator);
        compareField(logs, qc.getId(), "qcDate", "检测日期",
                old.getQcDate() == null ? null : old.getQcDate().toString(),
                qc.getQcDate() == null ? null : qc.getQcDate().toString(), operator);
        compareField(logs, qc.getId(), "remark", "备注", old.getRemark(), qc.getRemark(), operator);
        // 保存修改历史
        for (QcLog log : logs) {
            qcLogMapper.insert(log);
        }
        // 更新质检记录
        baseMapper.updateById(qc);
        // 回写产品批次质检状态
        if (qc.getQcResult() != null) {
            ProductBatch batch = productBatchMapper.selectById(qc.getBatchId());
            if (batch != null) {
                batch.setQcStatus(qc.getQcResult());
                productBatchMapper.updateById(batch);
            }
        }
    }

    @Override
    public List<QcLog> getQcLogs(Long qcId) {
        return qcLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QcLog>()
                        .eq(QcLog::getQcId, qcId)
                        .orderByDesc(QcLog::getCreateTime));
    }

    private void compareField(List<QcLog> logs, Long qcId, String fieldName, String fieldLabel,
                              String oldVal, String newVal, String operator) {
        if (newVal != null && !newVal.equals(oldVal)) {
            QcLog log = new QcLog();
            log.setQcId(qcId);
            log.setFieldName(fieldName);
            log.setFieldLabel(fieldLabel);
            log.setOldValue(oldVal);
            log.setNewValue(newVal);
            log.setOperator(operator);
            logs.add(log);
        }
    }
}
