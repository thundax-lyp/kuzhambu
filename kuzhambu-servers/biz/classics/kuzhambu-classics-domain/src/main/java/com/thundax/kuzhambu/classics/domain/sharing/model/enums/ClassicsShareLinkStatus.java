package com.thundax.kuzhambu.classics.domain.sharing.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsShareLinkStatus {
    ACTIVE,
    REVOKED,
    EXPIRED;

    public String value() {
        return name();
    }

    public static ClassicsShareLinkStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-14002", "classics.share.link.status.invalid", "Unknown classics share link status: " + value));
    }
}
