package com.thundax.kuzhambu.classics.domain.content.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsExportKind {
    CONTENT_DATASET,
    VISUAL_ASSET_DATASET;

    public String value() {
        return name();
    }

    public static ClassicsExportKind from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-13005", "classics.export.kind.invalid", "Unknown classics export kind: " + value));
    }
}
