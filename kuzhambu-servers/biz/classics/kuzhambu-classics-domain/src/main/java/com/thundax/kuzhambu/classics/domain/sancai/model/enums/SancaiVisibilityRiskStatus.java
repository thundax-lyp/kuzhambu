package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiVisibilityRiskStatus {
    PUBLIC_ONLY,
    CONTAINS_PRIVATE,
    PRIVATE_CONFIRMED;

    public String value() {
        return name();
    }

    public static SancaiVisibilityRiskStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10012",
                        "classics.visibility.risk.status.invalid",
                        "Unknown visibility risk status: " + value));
    }
}
