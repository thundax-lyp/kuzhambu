package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiVisualAssetStatus {
    DRAFT,
    PROCESSING,
    READY,
    FAILED;

    public String value() {
        return name();
    }

    public static SancaiVisualAssetStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10010", "classics.sancai.visual.asset.status.invalid", "Unknown sancai visual asset status: " + value));
    }
}
