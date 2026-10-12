package com.beefarm.warning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.base.entity.Hive;
import com.beefarm.base.mapper.HiveMapper;
import com.beefarm.common.exception.BusinessException;
import com.beefarm.common.utils.SecurityUtils;
import com.beefarm.disease.entity.Disease;
import com.beefarm.disease.entity.Medication;
import com.beefarm.disease.mapper.DiseaseMapper;
import com.beefarm.disease.mapper.MedicationMapper;
import com.beefarm.produce.entity.Harvest;
import com.beefarm.produce.entity.ProductBatch;
import com.beefarm.produce.mapper.HarvestMapper;
import com.beefarm.produce.mapper.ProductBatchMapper;
import com.beefarm.purchase.entity.Purchase;
import com.beefarm.purchase.mapper.PurchaseMapper;
import com.beefarm.sale.entity.Outbound;
import com.beefarm.sale.mapper.OutboundMapper;
import com.beefarm.stock.entity.Inventory;
import com.beefarm.stock.mapper.InventoryMapper;
import com.beefarm.warning.entity.Warning;
import com.beefarm.warning.mapper.WarningMapper;
import com.beefarm.warning.service.WarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警中心：列表 / 处理 / 按实时规则补录
 */
@Service
@RequiredArgsConstructor
public class WarningServiceImpl extends ServiceImpl<WarningMapper, Warning> implements WarningService {

    public static final String TYPE_WITHDRAW = "休药期";
    public static final String TYPE_STOCK = "库存不足";
    public static final String TYPE_HIVE = "蜂箱异常";
    public static final String TYPE_QC = "待质检";
    public static final String TYPE_AUDIT = "待审核";

    private final MedicationMapper medicationMapper;
    private final HarvestMapper harvestMapper;
    private final HiveMapper hiveMapper;
    private final InventoryMapper inventoryMapper;
    private final DiseaseMapper diseaseMapper;
    private final ProductBatchMapper productBatchMapper;
    private final OutboundMapper outboundMapper;
    private final PurchaseMapper purchaseMapper;

    @Override
    public IPage<Warning> pageList(Integer pageNum, Integer pageSize,
                                   String warningType, Integer status, Integer level) {
        LambdaQueryWrapper<Warning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warningType != null && !warningType.isBlank(),
                Warning::getWarningType, warningType);
        wrapper.eq(status != null, Warning::getStatus, status);
        wrapper.eq(level != null, Warning::getLevel, level);
        wrapper.orderByDesc(Warning::getLevel).orderByDesc(Warning::getId);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, String remark) {
        Warning w = baseMapper.selectById(id);
        if (w == null) {
            throw new BusinessException("预警不存在");
        }
        w.setStatus(1);
        w.setHandleBy(SecurityUtils.getUsername());
        w.setHandleTime(LocalDateTime.now());
        if (remark != null && !remark.isBlank()) {
            w.setRemark(remark);
        }
        baseMapper.updateById(w);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int regenerate() {
        int[] inserted = {0};
        LocalDate today = LocalDate.now();

        // 蜂箱编号映射
        Map<Long, String> hiveCodeMap = new HashMap<>();
        for (Hive h : hiveMapper.selectList(null)) {
            hiveCodeMap.put(h.getId(), h.getHiveCode());
        }

        // 1. 休药期：安全采收日仍在未来（含今天）
        for (Medication m : medicationMapper.selectList(null)) {
            if (m.getSafeDate() == null || m.getSafeDate().isBefore(today)) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(today, m.getSafeDate());
            int level = days <= 7 ? 3 : days <= 15 ? 2 : 1;
            String code = hiveCodeMap.getOrDefault(m.getHiveId(), "未知蜂箱");
            String content = String.format("蜂箱[%s]使用[%s]，休药期至%s，请暂缓采收",
                    code, m.getDrugName(), m.getSafeDate());
            if (dedupAndSave(TYPE_WITHDRAW, m.getId(), content, level)) {
                inserted[0]++;
            }
        }

        // 1b. 采收早于用药安全日（违规采收）
        List<Medication> meds = medicationMapper.selectList(null);
        for (Harvest hs : harvestMapper.selectList(null)) {
            for (Medication m : meds) {
                if (!m.getHiveId().equals(hs.getHiveId()) || m.getSafeDate() == null
                        || hs.getHarvestDate() == null || m.getUseDate() == null) {
                    continue;
                }
                if (!hs.getHarvestDate().isBefore(m.getSafeDate())
                        || hs.getHarvestDate().isBefore(m.getUseDate())) {
                    continue;
                }
                String content = String.format("采收批次[%s]采收日期%s早于用药[%s]安全采收日%s",
                        hs.getBatchNo(), hs.getHarvestDate(), m.getDrugName(), m.getSafeDate());
                if (dedupAndSave(TYPE_WITHDRAW, hs.getId(), content, 3)) {
                    inserted[0]++;
                }
            }
        }

        // 2. 库存不足
        for (Inventory i : inventoryMapper.selectList(null)) {
            if (i.getStock() == null || i.getWarnStock() == null
                    || i.getStock().compareTo(i.getWarnStock()) > 0) {
                continue;
            }
            int level = i.getStock().compareTo(BigDecimal.ZERO) <= 0 ? 3
                    : i.getStock().compareTo(i.getWarnStock().multiply(new BigDecimal("0.5"))) <= 0 ? 2 : 1;
            String content = String.format("库存物品[%s](%s)当前库存%s%s，低于预警值%s%s",
                    i.getItemName(), i.getSpec() == null ? "" : i.getSpec(),
                    i.getStock(), i.getUnit() == null ? "" : i.getUnit(),
                    i.getWarnStock(), i.getUnit() == null ? "" : i.getUnit());
            if (dedupAndSave(TYPE_STOCK, i.getId(), content, level)) {
                inserted[0]++;
            }
        }

        // 3a. 蜂箱异常（状态非正常）
        for (Hive h : hiveMapper.selectList(null)) {
            if (h.getStatus() != null && h.getStatus() == 1) {
                continue;
            }
            String statusName = switch (h.getStatus() == null ? 0 : h.getStatus()) {
                case 2 -> "空箱";
                case 3 -> "维修中";
                case 4 -> "已淘汰";
                default -> "未知状态";
            };
            int level = h.getStatus() != null && h.getStatus() == 4 ? 3 : 2;
            String content = String.format("蜂箱[%s]状态异常：%s", h.getHiveCode(), statusName);
            if (dedupAndSave(TYPE_HIVE, h.getId(), content, level)) {
                inserted[0]++;
            }
        }

        // 3b. 未处理病害
        for (Disease d : diseaseMapper.selectList(
                new LambdaQueryWrapper<Disease>().eq(Disease::getHandleStatus, 0))) {
            String code = hiveCodeMap.getOrDefault(d.getHiveId(), "未知蜂箱");
            String content = String.format("蜂箱[%s]存在未处理病害[%s]", code, d.getDiseaseType());
            if (dedupAndSave(TYPE_HIVE, d.getId(), content, d.getLevel() == null ? 2 : d.getLevel())) {
                inserted[0]++;
            }
        }

        // 4. 待质检
        for (ProductBatch p : productBatchMapper.selectList(
                new LambdaQueryWrapper<ProductBatch>().eq(ProductBatch::getQcStatus, 0))) {
            String content = String.format("产品批次[%s](%s)尚未质检",
                    p.getProductBatchNo(), p.getProductName() == null ? "" : p.getProductName());
            if (dedupAndSave(TYPE_QC, p.getId(), content, 2)) {
                inserted[0]++;
            }
        }

        // 5a. 待审核出库
        for (Outbound o : outboundMapper.selectList(
                new LambdaQueryWrapper<Outbound>().eq(Outbound::getStatus, 0))) {
            String content = String.format("出库单[%s]待审核", o.getOutboundNo());
            if (dedupAndSave(TYPE_AUDIT, o.getId(), content, 2)) {
                inserted[0]++;
            }
        }

        // 5b. 待审核采购
        for (Purchase p : purchaseMapper.selectList(
                new LambdaQueryWrapper<Purchase>().eq(Purchase::getStatus, 0))) {
            String content = String.format("采购单[%s]待审核", p.getPurchaseNo());
            if (dedupAndSave(TYPE_AUDIT, p.getId(), content, 2)) {
                inserted[0]++;
            }
        }

        return inserted[0];
    }

    /** 同类型+同业务ID存在未处理预警则跳过，避免重复补录 */
    private boolean dedupAndSave(String type, Long bizId, String content, int level) {
        Long exists = baseMapper.selectCount(new LambdaQueryWrapper<Warning>()
                .eq(Warning::getWarningType, type)
                .eq(Warning::getBizId, bizId)
                .eq(Warning::getStatus, 0));
        if (exists != null && exists > 0) {
            return false;
        }
        Warning w = new Warning();
        w.setWarningType(type);
        w.setBizId(bizId);
        w.setContent(content);
        w.setLevel(level);
        w.setStatus(0);
        w.setGenerateTime(LocalDateTime.now());
        baseMapper.insert(w);
        return true;
    }
}
