package com.beefarm.disease.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.beefarm.disease.entity.Medication;

public interface MedicationService extends IService<Medication> {
    IPage<Medication> pageList(Integer pageNum, Integer pageSize, Medication query);

    /** 新增/修改用药记录，自动计算安全采收日期 */
    void saveMedication(Medication medication);
}
