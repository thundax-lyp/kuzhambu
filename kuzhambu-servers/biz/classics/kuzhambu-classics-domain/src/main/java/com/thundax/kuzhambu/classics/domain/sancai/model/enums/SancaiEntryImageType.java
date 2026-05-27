package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiEntryImageType {
    ORIGINAL,
    GENERATED;

    public String value() {
        return name();
    }

    public static SancaiEntryImageType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10009", "classics.sancai.image.type.invalid", "Unknown sancai image type: " + value));
    }
}
