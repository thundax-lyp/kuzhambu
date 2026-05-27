package com.thundax.kuzhambu.system.application.auth.entity.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum PrincipalIdentityStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static PrincipalIdentityStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90006",
                        "auth.domain.principal-identity-status.invalid",
                        "Unknown principal identity status: " + value));
    }
}
