package com.thundax.kuzhambu.system.domain.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum LogType {
    ACCESS,
    EXCEPTION;

    public String value() {
        return name();
    }

    public static LogType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new DomainException("SYS-90001", "sys.domain.log-type.invalid", "Unknown log type: " + value));
    }
}
