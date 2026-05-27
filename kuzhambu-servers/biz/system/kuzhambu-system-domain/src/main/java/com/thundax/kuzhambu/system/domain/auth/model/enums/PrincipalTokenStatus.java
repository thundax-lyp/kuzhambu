package com.thundax.kuzhambu.system.domain.auth.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum PrincipalTokenStatus {
    ACTIVE,
    USED,
    REVOKED,
    EXPIRED;

    public String value() {
        return name();
    }

    public static PrincipalTokenStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90010",
                        "auth.domain.principal-token-status.invalid",
                        "Unknown principal token status: " + value));
    }
}
