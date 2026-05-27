package com.thundax.kuzhambu.classics.domain.content.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsExportStatus {
    REQUESTED,
    PROCESSING,
    COMPLETED,
    FAILED,
    EXPIRED,
    DELETED;

    public String value() {
        return name();
    }

    public static ClassicsExportStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-13008",
                        "classics.export.status.invalid",
                        "Unknown classics export status: " + value));
    }
}
