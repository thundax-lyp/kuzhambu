package com.thundax.kuzhambu.system.domain.core.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum RoleStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static RoleStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "SYS-90004", "sys.domain.role-status.invalid", "Unknown role status: " + value));
    }
}
