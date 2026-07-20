package com.thundax.kuzhambu.common.web.util;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public final class RequestIpUtils {

    private RequestIpUtils() {}

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        try {
            String remoteAddr = normalizeIp(request.getRemoteAddr());
            if (!isTrustedProxy(remoteAddr)) {
                return defaultString(remoteAddr);
            }
            String forwardedIp = firstValidForwardedIp(request.getHeader("x-forwarded-for"));
            if (isUnknown(forwardedIp)) {
                forwardedIp = firstValidForwardedIp(request.getHeader("X-Forwarded-For"));
            }
            if (isUnknown(forwardedIp)) {
                forwardedIp = normalizeIp(request.getHeader("X-Real-IP"));
            }
            if (isUnknown(forwardedIp)) {
                forwardedIp = normalizeIp(request.getHeader("Proxy-Client-IP"));
            }
            if (isUnknown(forwardedIp)) {
                forwardedIp = normalizeIp(request.getHeader("WL-Proxy-Client-IP"));
            }
            if (isUnknown(forwardedIp)) {
                forwardedIp = normalizeIp(request.getHeader("HTTP_CLIENT_IP"));
            }
            if (isUnknown(forwardedIp)) {
                forwardedIp = firstValidForwardedIp(request.getHeader("HTTP_X_FORWARDED_FOR"));
            }
            return isUnknown(forwardedIp) ? defaultString(remoteAddr) : forwardedIp;
        } catch (Exception e) {
            log.error("RequestIpUtils ERROR ", e);
            return "";
        }
    }

    private static String firstValidForwardedIp(String headerValue) {
        if (isUnknown(headerValue)) {
            return "";
        }
        String[] candidates = headerValue.split(",");
        for (String candidate : candidates) {
            String ip = normalizeIp(candidate);
            if (!isUnknown(ip)) {
                return ip;
            }
        }
        return "";
    }

    private static boolean isTrustedProxy(String ip) {
        InetAddress address = parseInetAddress(ip);
        if (address == null) {
            return false;
        }
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || isIpv6UniqueLocal(address);
    }

    private static boolean isIpv6UniqueLocal(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
    }

    private static String normalizeIp(String value) {
        if (isUnknown(value)) {
            return "";
        }
        String candidate = value.trim();
        if (candidate.startsWith("[") && candidate.endsWith("]")) {
            candidate = candidate.substring(1, candidate.length() - 1);
        }
        if (isValidIpv4(candidate) || isValidIpv6(candidate)) {
            return candidate;
        }
        return "";
    }

    private static boolean isValidIpv4(String value) {
        String[] parts = value.split("\\.", -1);
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3 || !StringUtils.isNumeric(part)) {
                return false;
            }
            int number = Integer.parseInt(part);
            if (number > 255) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidIpv6(String value) {
        return value.indexOf(':') >= 0 && value.matches("[0-9A-Fa-f:.%]+") && parseInetAddress(value) != null;
    }

    private static InetAddress parseInetAddress(String value) {
        if (isUnknown(value)) {
            return null;
        }
        try {
            return InetAddress.getByName(value);
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    private static boolean isUnknown(String ip) {
        return StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip);
    }
}
