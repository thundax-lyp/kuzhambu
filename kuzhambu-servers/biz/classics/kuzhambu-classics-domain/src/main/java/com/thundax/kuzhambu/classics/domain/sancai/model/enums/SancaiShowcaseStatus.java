package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiShowcaseStatus {
    REQUESTED,
    PROCESSING,
    COMPLETED,
    FAILED,
    EXPIRED;

    public String value() {
        return name();
    }

    public static SancaiShowcaseStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10011",
                        "classics.sancai.showcase.status.invalid",
                        "Unknown sancai showcase status: " + value));
    }
}
