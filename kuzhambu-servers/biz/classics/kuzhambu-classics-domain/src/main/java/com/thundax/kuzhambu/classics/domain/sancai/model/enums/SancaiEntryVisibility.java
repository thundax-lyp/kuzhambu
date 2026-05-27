package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiEntryVisibility {
    PUBLIC,
    PRIVATE;

    public String value() {
        return name();
    }

    public static SancaiEntryVisibility from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10004",
                        "classics.sancai.entry.visibility.invalid",
                        "Unknown sancai entry visibility: " + value));
    }
}
