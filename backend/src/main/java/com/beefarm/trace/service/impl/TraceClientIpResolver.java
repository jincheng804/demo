package com.beefarm.trace.service.impl;

import com.beefarm.common.utils.IpRegionUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TraceClientIpResolver {
    private final boolean trustLoopbackProxy;

    public TraceClientIpResolver(@Value("${bee.trace.trust-loopback-proxy:false}") boolean trustLoopbackProxy) {
        this.trustLoopbackProxy = trustLoopbackProxy;
    }

    String resolve(HttpServletRequest request) {
        String peer = request.getRemoteAddr();
        if (!trustLoopbackProxy || !"127.0.0.1".equals(peer)) {
            return peer;
        }

        // Nginx must overwrite X-Real-IP and overwrite or append its observed peer to XFF.
        String realIp = singleHeader(request, "X-Real-IP");
        if (IpRegionUtils.parseLiteral(realIp) != null) {
            return realIp;
        }
        String forwardedFor = singleHeader(request, "X-Forwarded-For");
        if (forwardedFor != null) {
            String lastHop = forwardedFor.substring(forwardedFor.lastIndexOf(',') + 1).strip();
            if (IpRegionUtils.parseLiteral(lastHop) != null) {
                return lastHop;
            }
        }
        return peer;
    }

    private String singleHeader(HttpServletRequest request, String name) {
        var values = request.getHeaders(name);
        if (values == null || !values.hasMoreElements()) {
            return null;
        }
        String value = values.nextElement();
        return values.hasMoreElements() ? null : value.strip();
    }
}
