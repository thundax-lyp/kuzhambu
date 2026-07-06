package com.thundax.kuzhambu.operations.domain.restore.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum RestoreMode {
    REAL,
    DRILL;

    public String value() {
        return name();
    }

    public static RestoreMode from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "OPERATIONS-RESTORE-400",
                        "operations.restore.mode.invalid",
                        "Unknown operations restore mode: " + value));
    }
}
