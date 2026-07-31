package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiEntryLifecycleStatus {
    DRAFT,
    PUBLISHED,
    OFFLINE,
    ERROR;

    public String value() {
        return name();
    }

    public boolean canChangeTo(SancaiEntryLifecycleStatus targetStatus) {
        return (this == DRAFT && targetStatus == PUBLISHED)
                || (this == PUBLISHED && targetStatus == OFFLINE)
                || (this == OFFLINE && targetStatus == PUBLISHED)
                || (this == ERROR && (targetStatus == PUBLISHED || targetStatus == OFFLINE));
    }

    public static SancaiEntryLifecycleStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10003",
                        "classics.sancai.entry.lifecycle.invalid",
                        "Unknown sancai entry lifecycle status: " + value));
    }
}
