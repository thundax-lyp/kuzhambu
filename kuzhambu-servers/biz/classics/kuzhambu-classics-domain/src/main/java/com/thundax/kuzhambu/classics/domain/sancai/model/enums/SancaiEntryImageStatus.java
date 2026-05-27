package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiEntryImageStatus {
    NONE,
    HAS_IMAGE,
    MISSING;

    public String value() {
        return name();
    }

    public static SancaiEntryImageStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10006", "classics.sancai.entry.image.invalid", "Unknown sancai entry image status: " + value));
    }
}
