package com.thundax.kuzhambu.system.application.auth.entity.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum PrincipalCredentialStatus {
    ACTIVE,
    LOCKED,
    EXPIRED,
    DISABLED;

    public String value() {
        return name();
    }

    public static PrincipalCredentialStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90003",
                        "auth.domain.principal-credential-status.invalid",
                        "Unknown principal credential status: " + value));
    }
}
