package com.thundax.kuzhambu.classics.domain.content.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsContentTagStatus {
    ACTIVE,
    REMOVED;

    public String value() {
        return name();
    }

    public static ClassicsContentTagStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-13003",
                        "classics.content.tag.status.invalid",
                        "Unknown classics content tag status: " + value));
    }
}
