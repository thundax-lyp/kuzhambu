package com.thundax.kuzhambu.classics.domain.sharing.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsShareAccessResult {
    ALLOWED,
    EXPIRED,
    REVOKED,
    FORBIDDEN,
    NOT_FOUND;

    public String value() {
        return name();
    }

    public static ClassicsShareAccessResult from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-14005",
                        "classics.share.access.result.invalid",
                        "Unknown classics share access result: " + value));
    }
}
