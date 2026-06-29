package com.thundax.kuzhambu.operations.domain.backup.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum BackupStatus {
    RUNNING,
    SUCCEEDED,
    FAILED;

    public String value() {
        return name();
    }

    public static BackupStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "OPERATIONS-BACKUP-400",
                        "operations.backup.status.invalid",
                        "Unknown operations backup status: " + value));
    }
}
