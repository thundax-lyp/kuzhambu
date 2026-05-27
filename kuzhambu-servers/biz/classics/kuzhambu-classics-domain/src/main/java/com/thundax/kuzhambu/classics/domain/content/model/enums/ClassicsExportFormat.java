package com.thundax.kuzhambu.classics.domain.content.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsExportFormat {
    CSV,
    JSON,
    HTML;

    public String value() {
        return name();
    }

    public static ClassicsExportFormat from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-13006",
                        "classics.export.format.invalid",
                        "Unknown classics export format: " + value));
    }
}
