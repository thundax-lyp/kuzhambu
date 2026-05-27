package com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum MingCustomsContentFormat {
    TEXT,
    MARKDOWN;

    public String value() {
        return name();
    }

    public static MingCustomsContentFormat from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-12001", "classics.mingcustoms.content.format.invalid", "Unknown ming customs content format: " + value));
    }
}
