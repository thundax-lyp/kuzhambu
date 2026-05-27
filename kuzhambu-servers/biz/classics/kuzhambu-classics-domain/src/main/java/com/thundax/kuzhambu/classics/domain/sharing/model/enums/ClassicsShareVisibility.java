package com.thundax.kuzhambu.classics.domain.sharing.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsShareVisibility {
    PUBLIC,
    PRIVATE;

    public String value() {
        return name();
    }

    public static ClassicsShareVisibility from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-14001", "classics.share.visibility.invalid", "Unknown classics share visibility: " + value));
    }
}
