package com.beefarm.common.utils;

import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.DataBlock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    /** 仅接受 IPv4 或 IPv6 字面地址，拒绝主机名、端口、地址列表及 IPv6 区域 ID。 */
    public static InetAddress parseLiteral(String ip) {
        if (ip == null || ip.length() > 45) {
            return null;
        }
        try {
            if (ip.indexOf(':') >= 0) {
                return ip.matches("[0-9a-fA-F:.]+") ? InetAddress.getByName(ip) : null;
            }
            if (!ip.matches("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}")) {
                return null;
            }
            String[] parts = ip.split("\\.");
            byte[] address = new byte[4];
            for (int i = 0; i < address.length; i++) {
                int octet = Integer.parseInt(parts[i]);
                if (octet > 255) {
                    return null;
                }
                address[i] = (byte) octet;
            }
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /** 根据 IP 查询归属地，无法定位时给出明确的显示值。 */
    public static String getRegion(String ip) {
        InetAddress address = parseLiteral(ip);
        if ("localhost".equalsIgnoreCase(ip) || (address != null && (
                address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()
                        || (address.getAddress().length == 16 && (address.getAddress()[0] & 0xfe) == 0xfc)))) {
            return "内网 / 未知";
        }
        // 当前随程序分发的地区库只支持 IPv4。
        if (!(address instanceof Inet4Address)) {
            return "未知";
        }
        try {
            DbSearcher searcher = SEARCHER_HOLDER.get();
            if (searcher == null) {
                searcher = new DbSearcher(new DbConfig(), ensureDbFile().getAbsolutePath());
                SEARCHER_HOLDER.set(searcher);
            }
            DataBlock block = searcher.btreeSearch(address.getHostAddress());
            if (block == null) {
                return "未知";
            }
            String region = block.getRegion();
            if (!StringUtils.hasText(region)) {
                return "未知";
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
            return result.isEmpty() ? "未知" : result;
        } catch (Exception e) {
            return "未知";
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
