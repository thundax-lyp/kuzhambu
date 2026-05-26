package com.thundax.kuzhambu.biz.storage.entity.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum StorageOwnerType {
    USER,
    MEMBER,
    SUBMISSION;

    public String value() {
        return name();
    }

    public static StorageOwnerType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "STORAGE-90002", "storage.domain.owner-type.invalid", "Unknown storage owner type: " + value));
    }
}
