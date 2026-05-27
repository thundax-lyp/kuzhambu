package com.thundax.kuzhambu.system.application.auth.entity.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum PrincipalLoginEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    TOKEN_REFRESH,
    OAUTH_AUTHORIZED;

    public String value() {
        return name();
    }

    public static PrincipalLoginEventType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90009",
                        "auth.domain.principal-login-event-type.invalid",
                        "Unknown principal login event type: " + value));
    }
}
