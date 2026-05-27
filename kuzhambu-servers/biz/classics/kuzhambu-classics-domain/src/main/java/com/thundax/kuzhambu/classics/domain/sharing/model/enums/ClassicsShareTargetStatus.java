package com.thundax.kuzhambu.classics.domain.sharing.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsShareTargetStatus {
    AVAILABLE,
    CONTENT_DELETED;

    public String value() {
        return name();
    }

    public static ClassicsShareTargetStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-14004",
                        "classics.share.target.status.invalid",
                        "Unknown classics share target status: " + value));
    }
}
