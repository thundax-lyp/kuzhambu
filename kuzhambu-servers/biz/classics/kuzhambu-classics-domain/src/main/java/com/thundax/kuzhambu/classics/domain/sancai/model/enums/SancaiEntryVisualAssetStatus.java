package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiEntryVisualAssetStatus {
    NONE,
    PROCESSING,
    READY,
    FAILED;

    public String value() {
        return name();
    }

    public static SancaiEntryVisualAssetStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10007", "classics.sancai.entry.visual.asset.invalid", "Unknown sancai entry visual asset status: " + value));
    }
}
