package com.beefarm.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.base.entity.Hive;
import com.beefarm.base.entity.NectarSource;
import com.beefarm.base.mapper.HiveMapper;
import com.beefarm.base.mapper.NectarSourceMapper;
import com.beefarm.common.exception.BusinessException;
import com.beefarm.common.utils.QrCodeUtils;
import com.beefarm.disease.entity.Medication;
import com.beefarm.disease.mapper.MedicationMapper;
import com.beefarm.produce.entity.Harvest;
import com.beefarm.produce.entity.ProductBatch;
import com.beefarm.produce.entity.QcRecord;
import com.beefarm.produce.mapper.HarvestMapper;
import com.beefarm.produce.mapper.ProductBatchMapper;
import com.beefarm.produce.mapper.QcRecordMapper;
import com.beefarm.trace.entity.ScanLog;
import com.beefarm.trace.entity.TraceCode;
import com.beefarm.trace.mapper.ScanLogMapper;
import com.beefarm.trace.mapper.TraceCodeMapper;
import com.beefarm.trace.service.TraceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TraceServiceImpl extends ServiceImpl<TraceCodeMapper, TraceCode>
        implements TraceService {

    private final ProductBatchMapper productBatchMapper;
    private final HarvestMapper harvestMapper;
    private final HiveMapper hiveMapper;
    private final NectarSourceMapper nectarSourceMapper;
    private final MedicationMapper medicationMapper;
    private final QcRecordMapper qcRecordMapper;
    private final ScanLogMapper scanLogMapper;
    private final com.beefarm.common.service.BusinessGuard businessGuard;

    @Value("${bee.trace.base-url}")
    private String baseUrl;


    @jakarta.annotation.PostConstruct
    public void validateBaseUrl() {
        try {
            baseUrl = baseUrl.trim().replaceAll("/+$", "");
            java.net.URI uri = java.net.URI.create(baseUrl);
            if (!java.util.Set.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            throw new IllegalStateException("BEE_TRACE_BASE_URL 须为有效的 HTTP/HTTPS 前端基础地址", e);
        }
    }

    private String traceUrl(String code) {
        return baseUrl + "/trace/" + code;
    }

    @Override
    public IPage<TraceCode> pageList(Integer pageNum, Integer pageSize, TraceCode query) {
        IPage<TraceCode> page = baseMapper.selectTracePage(new Page<>(pageNum, pageSize), query);
        page.getRecords().forEach(trace -> trace.setQrUrl(traceUrl(trace.getTraceCode())));
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TraceCode generate(Long productBatchId) {
        businessGuard.lock();
        ProductBatch batch = productBatchMapper.selectById(productBatchId);
        if (batch == null) {
            throw new BusinessException("产品批次不存在");
        }
        // 一个产品批次可生成多个溯源码(每瓶一码)，这里按时间生成唯一码
        String code = "TR" + java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase(java.util.Locale.ROOT);
        TraceCode trace = new TraceCode();
        trace.setTraceCode(code);
        trace.setProductBatchId(productBatchId);
        trace.setQrUrl(traceUrl(code));
        trace.setScanCount(0);
        baseMapper.insert(trace);
        trace.setProductBatchNo(batch.getProductBatchNo());
        trace.setProductName(batch.getProductName());
        trace.setSpec(batch.getSpec());
        return trace;
    }

    @Override
    public byte[] qrImage(String traceCode) {
        TraceCode trace = findByCode(traceCode);
        return QrCodeUtils.generate(traceUrl(trace.getTraceCode()), 300, 300);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> publicTrace(String traceCode, HttpServletRequest request) {
        TraceCode trace = findByCode(traceCode);
        if (request != null) {
            baseMapper.incrementScan(trace.getId());
            trace = baseMapper.selectById(trace.getId());
            saveScanLog(trace.getTraceCode(), request);
        }

        ProductBatch product = productBatchMapper.selectById(trace.getProductBatchId());
        if (product == null) {
            throw new BusinessException("产品信息缺失");
        }
        Harvest harvest = harvestMapper.selectById(product.getHarvestId());
        Hive hive = harvest != null ? hiveMapper.selectById(harvest.getHiveId()) : null;
        NectarSource source = harvest != null && harvest.getSourceId() != null
                ? nectarSourceMapper.selectById(harvest.getSourceId()) : null;
        List<Medication> medications = hive != null
                ? medicationMapper.selectList(new LambdaQueryWrapper<Medication>()
                .eq(Medication::getHiveId, hive.getId()).le(Medication::getUseDate, harvest.getHarvestDate())) : List.of();
        List<QcRecord> qcRecords = qcRecordMapper.selectList(new LambdaQueryWrapper<QcRecord>()
                .eq(QcRecord::getBatchId, product.getId()));

        return Map.of(
                "traceCode", trace.getTraceCode(),
                "product", product,
                "harvest", harvest == null ? Map.of() : harvest,
                "hive", hive == null ? Map.of() : hive,
                "source", source == null ? Map.of() : source,
                "medications", medications,
                "qcRecords", qcRecords,
                "scanCount", trace.getScanCount(),
                "queryTime", LocalDateTime.now()
        );
    }

    private TraceCode findByCode(String code) {
        TraceCode trace = baseMapper.selectOne(new LambdaQueryWrapper<TraceCode>()
                .eq(TraceCode::getTraceCode, code));
        if (trace == null) {
            throw new BusinessException("溯源码无效或不存在");
        }
        return trace;
    }

    /** 写一条扫码日志 */
    private void saveScanLog(String traceCode, HttpServletRequest request) {
        ScanLog scanLog = new ScanLog();
        scanLog.setTraceCode(traceCode);
        scanLog.setScanTime(LocalDateTime.now());
        scanLog.setIp(resolveClientIp(request));
        String ua = request.getHeader("User-Agent");
        scanLog.setUserAgent(ua == null ? null : ua.substring(0, Math.min(255, ua.length())));
        scanLog.setDeviceType(inferDeviceType(ua));
        scanLogMapper.insert(scanLog);
    }

    /** 提取客户端真实IP(反向代理优先) */
    private String resolveClientIp(HttpServletRequest request) {
        // Only trust the connection address; arbitrary client forwarding headers can be forged.
        return request.getRemoteAddr();
    }

    /** 根据UA推断设备类型 */
    private String inferDeviceType(String ua) {
        if (ua == null) {
            return "浏览器";
        }
        if (ua.contains("MicroMessenger")) {
            return "微信";
        }
        if (ua.contains("Android")) {
            return "Android";
        }
        if (ua.contains("iPhone")) {
            return "iPhone";
        }
        if (ua.contains("iPad")) {
            return "iPad";
        }
        if (ua.contains("Mobile")) {
            return "手机";
        }
        return "浏览器";
    }
}