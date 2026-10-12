package com.beefarm.produce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.common.exception.BusinessException;
import com.beefarm.disease.entity.Medication;
import com.beefarm.disease.mapper.MedicationMapper;
import com.beefarm.produce.entity.Harvest;
import com.beefarm.produce.mapper.HarvestMapper;
import com.beefarm.produce.service.HarvestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HarvestServiceImpl extends ServiceImpl<HarvestMapper, Harvest> implements HarvestService {
    private final com.beefarm.common.service.BusinessGuard businessGuard;

    private final MedicationMapper medicationMapper;
    private final com.beefarm.produce.mapper.ProductBatchMapper productMapper;

    @Override
    public IPage<Harvest> pageList(Integer pageNum, Integer pageSize, Harvest query) {
        return baseMapper.selectHarvestPage(new Page<>(pageNum, pageSize), query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addHarvest(Harvest harvest) {
        businessGuard.lock();
        if (harvest.getHiveId() == null) {
            throw new BusinessException("请选择蜂箱");
        }
        if (harvest.getHarvestDate() == null) {
            harvest.setHarvestDate(LocalDate.now());
        }
        com.beefarm.common.utils.BusinessChecks.require(harvest.getId() == null, "新增记录不能指定ID");
        com.beefarm.common.utils.BusinessChecks.quantity(harvest.getWeight());
        harvest.setStatus(1);
        // 休药期校验：该蜂箱存在用药记录，且摇蜜日期早于安全采收日期则拦截
        List<Medication> meds = medicationMapper.selectList(
                new LambdaQueryWrapper<Medication>()
                        .eq(Medication::getHiveId, harvest.getHiveId())
                        .le(Medication::getUseDate, harvest.getHarvestDate())
                        .isNotNull(Medication::getSafeDate)
                        .gt(Medication::getSafeDate, harvest.getHarvestDate()));
        if (!meds.isEmpty()) {
            Medication m = meds.get(0);
            throw new BusinessException("该蜂箱处于休药期内，药品【" + m.getDrugName()
                    + "】安全采收日期为 " + m.getSafeDate() + "，暂不可摇蜜！");
        }
        // 生成采收批次号 HS + yyyyMMdd + 4位流水
        String batchNo = businessGuard.nextNumber("HS", harvest.getHarvestDate());
        harvest.setBatchNo(batchNo);
        if (harvest.getStatus() == null) {
            harvest.setStatus(1);
        }
        baseMapper.insert(harvest);
        return batchNo;
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void updateHarvest(Harvest update) {
        businessGuard.lock();
        Harvest current = baseMapper.selectById(update.getId());
        com.beefarm.common.utils.BusinessChecks.require(current != null, "采收批次不存在");
        com.beefarm.common.utils.BusinessChecks.require(productMapper.selectCount(new LambdaQueryWrapper<com.beefarm.produce.entity.ProductBatch>()
                .eq(com.beefarm.produce.entity.ProductBatch::getHarvestId, update.getId())) == 0, "已装瓶的采收记录不能修改");
        if (update.getHiveId() == null) update.setHiveId(current.getHiveId());
        if (update.getHarvestDate() == null) update.setHarvestDate(current.getHarvestDate());
        if (update.getWeight() == null) update.setWeight(current.getWeight());
        com.beefarm.common.utils.BusinessChecks.quantity(update.getWeight());
        com.beefarm.common.utils.BusinessChecks.require(medicationMapper.selectCount(new LambdaQueryWrapper<Medication>()
                .eq(Medication::getHiveId, update.getHiveId()).le(Medication::getUseDate, update.getHarvestDate())
                .gt(Medication::getSafeDate, update.getHarvestDate())) == 0, "该日期处于休药期内，不能采收");
        update.setBatchNo(current.getBatchNo());
        update.setStatus(current.getStatus());
        baseMapper.updateById(update);
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void deleteHarvest(Long id) {
        businessGuard.lock();
        com.beefarm.common.utils.BusinessChecks.require(productMapper.selectCount(new LambdaQueryWrapper<com.beefarm.produce.entity.ProductBatch>()
                .eq(com.beefarm.produce.entity.ProductBatch::getHarvestId, id)) == 0, "采收批次已关联产品，不能删除");
        baseMapper.deleteById(id);
    }

}
