package com.thundax.kuzhambu.system.application.auth.entity.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum PrincipalAuthenticationMethod {
    PASSWORD,
    SMS_CODE,
    EMAIL_CODE,
    GITHUB,
    WECOM,
    OAUTH_CODE,
    REFRESH_TOKEN;

    public String value() {
        return name();
    }

    public static PrincipalAuthenticationMethod from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90002",
                        "auth.domain.principal-authentication-method.invalid",
                        "Unknown principal authentication method: " + value));
    }
}
