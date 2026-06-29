package com.thundax.kuzhambu.operations.domain.backup.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum BackupType {
    MANUAL("backup"),
    AUTO("backup"),
    PRE_RESTORE("prerestore");

    private final String filePrefix;

    BackupType(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public String value() {
        return name();
    }

    public String filePrefix() {
        return filePrefix;
    }

    public static BackupType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "OPERATIONS-BACKUP-400",
                        "operations.backup.type.invalid",
                        "Unknown operations backup type: " + value));
    }
}
