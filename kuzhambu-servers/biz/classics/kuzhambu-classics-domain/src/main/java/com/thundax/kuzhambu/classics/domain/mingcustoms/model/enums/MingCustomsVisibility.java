package com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum MingCustomsVisibility {
    PUBLIC,
    PRIVATE;

    public String value() {
        return name();
    }

    public static MingCustomsVisibility from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-12002", "classics.mingcustoms.visibility.invalid", "Unknown ming customs visibility: " + value));
    }
}
