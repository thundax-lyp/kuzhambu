package com.thundax.kuzhambu.common.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class RequestIpUtilsTest {

    @Test
    public void shouldPreferRealIpHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "10.0.0.1");
        request.addHeader("x-forwarded-for", "10.0.0.2");

        assertEquals("10.0.0.1", RequestIpUtils.getIpAddr(request));
    }

    @Test
    public void shouldFallbackToForwardedForHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
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
    public void shouldReturnEmptyIpWhenRequestIsNull() {
        assertEquals("", RequestIpUtils.getIpAddr(null));
    }
}
