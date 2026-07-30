package com.thundax.kuzhambu.discovery.interfaces.common;

import com.thundax.kuzhambu.common.web.exception.BadRequestException;
import org.apache.commons.lang3.StringUtils;

public final class DiscoveryInterfaceIdCodec {

    private DiscoveryInterfaceIdCodec() {}

    public static String toStringValue(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    public static Long toLongValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            Long parsedValue = Long.valueOf(value.trim());
            if (parsedValue <= 0L) {
                throw new BadRequestException("标识必须为有效正整数");
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            throw new BadRequestException("标识必须为有效数字");
        }
    }
}
