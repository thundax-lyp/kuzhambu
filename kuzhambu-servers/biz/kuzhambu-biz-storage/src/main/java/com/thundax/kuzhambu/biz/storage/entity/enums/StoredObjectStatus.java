package com.thundax.kuzhambu.biz.storage.entity.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum StoredObjectStatus {
    ACTIVE,
    DELETING,
    DELETED;

    public String value() {
        return name();
    }

    public static StoredObjectStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "STORAGE-90005", "storage.domain.status.invalid", "Unknown storage status: " + value));
    }
}
