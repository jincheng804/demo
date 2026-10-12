package com.beefarm.disease.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.disease.entity.Medication;
import com.beefarm.disease.mapper.MedicationMapper;
import com.beefarm.disease.service.MedicationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MedicationServiceImpl extends ServiceImpl<MedicationMapper, Medication>
        implements MedicationService {

    @Override
    public IPage<Medication> pageList(Integer pageNum, Integer pageSize, Medication query) {
        return baseMapper.selectMedicationPage(new Page<>(pageNum, pageSize), query);
    }

    @Override
    public void saveMedication(Medication m) {
        // 自动计算安全采收日期 = 用药日期 + 休药期
        int days = m.getWithdrawalDays() == null ? 0 : m.getWithdrawalDays();
        if (m.getUseDate() != null) {
            m.setSafeDate(m.getUseDate().plusDays(days));
        }
        if (m.getId() == null) {
            baseMapper.insert(m);
        } else {
            baseMapper.updateById(m);
        }
    }
}
