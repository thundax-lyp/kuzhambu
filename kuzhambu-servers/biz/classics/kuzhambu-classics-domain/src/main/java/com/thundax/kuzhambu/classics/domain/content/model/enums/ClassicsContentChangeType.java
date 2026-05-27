package com.thundax.kuzhambu.classics.domain.content.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsContentChangeType {
    MANUAL_SAVE,
    AI_APPLIED,
    HISTORY_RESTORED;

    public String value() {
        return name();
    }

    public static ClassicsContentChangeType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-13004",
                        "classics.content.change.type.invalid",
                        "Unknown classics content change type: " + value));
    }
}
