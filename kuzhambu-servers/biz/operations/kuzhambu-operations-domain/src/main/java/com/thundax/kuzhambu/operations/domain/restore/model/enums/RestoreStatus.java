package com.thundax.kuzhambu.operations.domain.restore.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum RestoreStatus {
    RUNNING,
    SUCCEEDED,
    FAILED;

    public String value() {
        return name();
    }

    public static RestoreStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "OPERATIONS-RESTORE-400",
                        "operations.restore.status.invalid",
                        "Unknown operations restore status: " + value));
    }
}
