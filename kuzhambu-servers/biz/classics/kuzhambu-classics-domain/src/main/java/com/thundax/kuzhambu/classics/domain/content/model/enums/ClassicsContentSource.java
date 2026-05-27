package com.thundax.kuzhambu.classics.domain.content.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsContentSource {
    MANUAL,
    AI;

    public String value() {
        return name();
    }

    public static ClassicsContentSource from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-13002", "classics.content.source.invalid", "Unknown classics content source: " + value));
    }
}
