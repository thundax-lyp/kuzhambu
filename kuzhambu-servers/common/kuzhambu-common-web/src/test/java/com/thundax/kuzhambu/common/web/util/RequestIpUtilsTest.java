package com.thundax.kuzhambu.common.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class RequestIpUtilsTest {

    @Test
    public void shouldPreferRealIpHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.10");
        request.addHeader("X-Real-IP", "10.0.0.1");
        request.addHeader("x-forwarded-for", "10.0.0.2");

        assertEquals("10.0.0.2", RequestIpUtils.getIpAddr(request));
    }

    @Test
    public void shouldFallbackToForwardedForHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.10");
        request.addHeader("X-Real-IP", "unknown");
        request.addHeader("x-forwarded-for", "10.0.0.2");

        assertEquals("10.0.0.2", RequestIpUtils.getIpAddr(request));
    }

    @Test
    public void shouldFallbackToRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        assertEquals("127.0.0.1", RequestIpUtils.getIpAddr(request));
    }

    @Test
    public void shouldIgnoreForwardedHeaderFromUntrustedRemoteAddress() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("x-forwarded-for", "10.0.0.2");

        assertEquals("203.0.113.10", RequestIpUtils.getIpAddr(request));
    }

    @Test
    public void shouldUseFirstValidForwardedIpFromTrustedProxy() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.10");
        request.addHeader("x-forwarded-for", "bad-value, 198.51.100.20, unknown");

        assertEquals("198.51.100.20", RequestIpUtils.getIpAddr(request));
    }

    @Test
    public void shouldFallbackToRemoteAddrWhenForwardedHeaderIsInvalid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.10");
        request.addHeader("x-forwarded-for", "bad-value, unknown");

        assertEquals("192.168.1.10", RequestIpUtils.getIpAddr(request));
    }

    @Test
    public void shouldReturnEmptyIpWhenRequestIsNull() {
        assertEquals("", RequestIpUtils.getIpAddr(null));
    }
}
