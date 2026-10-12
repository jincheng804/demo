package com.beefarm.common.utils;

import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.DataBlock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * IP归属地离线查询工具（ip2region）
 * 数据文件: ip2region.db（内置classpath，首次使用时解压到临时目录）
 */
public class IpRegionUtils {

    private static volatile String dbPath;

    /** 每线程独立Searcher（DbSearcher非线程安全） */
    private static final ThreadLocal<DbSearcher> SEARCHER_HOLDER = new ThreadLocal<>();

    /**
     * 根据IP查询归属地，返回简化结果，如：中国 浙江省 杭州市
     * 内网地址/查询失败返回null
     */
    public static String getRegion(String ip) {
        if (!StringUtils.hasText(ip) || "127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return null;
        }
        try {
            DbSearcher searcher = SEARCHER_HOLDER.get();
            if (searcher == null) {
                searcher = new DbSearcher(new DbConfig(), ensureDbFile().getAbsolutePath());
                SEARCHER_HOLDER.set(searcher);
            }
            DataBlock block = searcher.btreeSearch(ip);
            if (block == null) {
                return null;
            }
            String region = block.getRegion();
            if (!StringUtils.hasText(region)) {
                return null;
            }
            // 原始格式: 国家|区域|省份|城市|运营商
            String[] parts = region.split("\\|");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                if (p == null || p.isEmpty() || "0".equals(p)) {
                    continue;
                }
                if ("内网IP".equals(p) || "纯真网络".equals(p)) {
                    continue;
                }
                sb.append(p).append(' ');
            }
            String result = sb.toString().trim();
            return result.isEmpty() ? region : result;
        } catch (Exception e) {
            return null;
        }
    }

    private static File ensureDbFile() throws Exception {
        if (dbPath == null) {
            synchronized (IpRegionUtils.class) {
                if (dbPath == null) {
                    ClassPathResource res = new ClassPathResource("ip2region.db");
                    Path tmp = Files.createTempFile("ip2region_", ".db");
                    try (InputStream in = res.getInputStream()) {
                        Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                    }
                    tmp.toFile().deleteOnExit();
                    dbPath = tmp.toString();
                }
            }
        }
        return new File(dbPath);
    }
}
