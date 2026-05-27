package com.thundax.kuzhambu.system.domain.auth.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum PrincipalType {
    USER,
    MEMBER,
    OPEN_CLIENT;

    public String value() {
        return name();
    }

    public static PrincipalType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90011", "auth.domain.principal-type.invalid", "Unknown principal type: " + value));
    }
}
