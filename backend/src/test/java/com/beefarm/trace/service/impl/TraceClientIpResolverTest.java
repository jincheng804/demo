package com.beefarm.trace.service.impl;

import com.beefarm.common.utils.IpRegionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TraceClientIpResolverTest {
    @Test
    void ignoresForwardingHeadersUnlessLoopbackProxyIsExplicitlyTrusted() {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Real-IP", "203.0.113.7");
        request.addHeader("X-Forwarded-For", "203.0.113.8");
        assertEquals("127.0.0.1", new TraceClientIpResolver(false).resolve(request));

        request.setRemoteAddr("192.0.2.10");
        assertEquals("192.0.2.10", new TraceClientIpResolver(true).resolve(request));
    }

    @Test
    void trustedProxyPrefersRealIpAndUsesOnlyRightmostForwardedAddress() {
        var resolver = new TraceClientIpResolver(true);
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "192.0.2.66, 203.0.113.8");
        assertEquals("203.0.113.8", resolver.resolve(request));

        request.addHeader("X-Real-IP", "2001:db8::1");
        assertEquals("2001:db8::1", resolver.resolve(request));
    }

    @Test
    void malformedProxyHeadersFallBackToPeerAndLocalRegionsHaveAPlaceholder() {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Real-IP", "999.1.2.3");
        request.addHeader("X-Forwarded-For", "192.0.2.66, example.test");
        var resolver = new TraceClientIpResolver(true);
        assertEquals("127.0.0.1", resolver.resolve(request));
        assertEquals("内网 / 未知", IpRegionUtils.getRegion("127.0.0.1"));
        assertEquals("内网 / 未知", IpRegionUtils.getRegion("192.168.1.10"));
        assertEquals("内网 / 未知", IpRegionUtils.getRegion("fc00::1"));
        assertEquals("未知", IpRegionUtils.getRegion("example.test"));
    }
}
