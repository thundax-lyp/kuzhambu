package com.thundax.kuzhambu.classics.domain.content.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsContentType {
    SANCAI_ENTRY,
    WANGQI_DOCUMENT,
    MING_CUSTOMS;

    public String value() {
        return name();
    }

    public static ClassicsContentType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-13001", "classics.content.type.invalid", "Unknown classics content type: " + value));
    }
}
