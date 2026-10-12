package com.beefarm.disease.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beefarm.disease.entity.Medication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MedicationMapper extends BaseMapper<Medication> {
    IPage<Medication> selectMedicationPage(IPage<Medication> page, @Param("medication") Medication medication);
}
