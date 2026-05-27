package com.thundax.kuzhambu.classics.domain.sharing.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsSharedContentVisibility {
    PUBLIC,
    PRIVATE;

    public String value() {
        return name();
    }

    public static ClassicsSharedContentVisibility from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-14003",
                        "classics.shared.content.visibility.invalid",
                        "Unknown shared content visibility: " + value));
    }
}
