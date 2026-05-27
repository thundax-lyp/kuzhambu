package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiEntryTranslationStatus {
    UNTRANSLATED,
    TRANSLATING,
    TRANSLATED;

    public String value() {
        return name();
    }

    public static SancaiEntryTranslationStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10005",
                        "classics.sancai.entry.translation.invalid",
                        "Unknown sancai entry translation status: " + value));
    }
}
