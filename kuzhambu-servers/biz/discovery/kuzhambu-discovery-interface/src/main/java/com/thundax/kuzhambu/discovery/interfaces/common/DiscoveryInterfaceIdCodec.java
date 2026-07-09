package com.thundax.kuzhambu.discovery.interfaces.common;

import org.apache.commons.lang3.StringUtils;

public final class DiscoveryInterfaceIdCodec {

    private DiscoveryInterfaceIdCodec() {}

    public static String toStringValue(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    public static Long toLongValue(String value) {
        return StringUtils.isBlank(value) ? null : Long.valueOf(value);
    }
}
