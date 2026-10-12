package com.beefarm.common.service;
import com.beefarm.common.mapper.BusinessGuardMapper;
import com.beefarm.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/** Single-farm stock writes are serialized in the database, including multiple app instances. */
@Service
@RequiredArgsConstructor
public class BusinessGuard {
    private final BusinessGuardMapper mapper;
    @Transactional(propagation = Propagation.MANDATORY)
    public void lock() {
        if (mapper.lock() == null) throw new BusinessException("业务锁未初始化，请执行数据库升级脚本");
    }
    @Transactional(propagation = Propagation.MANDATORY)
    public String nextNumber(String kind, LocalDate date) {
        if (!java.util.Set.of("HS", "CP", "CK", "CG").contains(kind)) throw new IllegalArgumentException("number kind");
        lock();
        String prefix = kind + date.format(DateTimeFormatter.BASIC_ISO_DATE);
        int next = mapper.maxSequence(prefix) + 1;
        if (next > 9999) throw new BusinessException("当日单据编号已用尽");
        return prefix + String.format("%04d", next);
    }
}
