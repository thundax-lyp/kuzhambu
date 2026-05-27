package com.thundax.kuzhambu.system.domain.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum RolePrivilege {
    NORMAL,
    ADMIN;

    public String value() {
        return name();
    }

    public static RolePrivilege from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "SYS-90003", "sys.domain.role-privilege.invalid", "Unknown role privilege: " + value));
    }
}
