package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiEntryRefinementStatus {
    TODO,
    IN_PROGRESS,
    COMPLETE;

    public String value() {
        return name();
    }

    public static SancaiEntryRefinementStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10008",
                        "classics.sancai.entry.refinement.invalid",
                        "Unknown sancai entry refinement status: " + value));
    }
}
