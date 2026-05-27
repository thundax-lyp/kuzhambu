package com.thundax.kuzhambu.system.domain.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum PrincipalCredentialType {
    USER_PASSWORD(PrincipalType.USER, "PASSWORD"),
    MEMBER_PASSWORD(PrincipalType.MEMBER, "PASSWORD"),
    API_SECRET(PrincipalType.OPEN_CLIENT, "API_SECRET");

    private final PrincipalType principalType;
    private final String credentialName;

    PrincipalCredentialType(PrincipalType principalType, String credentialName) {
        this.principalType = principalType;
        this.credentialName = credentialName;
    }

    public String value() {
        return name();
    }

    public PrincipalType principalType() {
        return principalType;
    }

    public String credentialName() {
        return credentialName;
    }

    public boolean isUserPrincipal() {
        return PrincipalType.USER == principalType;
    }

    public boolean isMemberPrincipal() {
        return PrincipalType.MEMBER == principalType;
    }

    public boolean isOpenClientPrincipal() {
        return PrincipalType.OPEN_CLIENT == principalType;
    }

    public boolean isPassword() {
        return "PASSWORD".equals(credentialName);
    }

    public boolean isApiSecret() {
        return "API_SECRET".equals(credentialName);
    }

    public static PrincipalCredentialType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90004",
                        "auth.domain.principal-credential-type.invalid",
                        "Unknown principal credential type: " + value));
    }

    public static PrincipalCredentialType from(PrincipalType principalType, String credentialName) {
        return Arrays.stream(values())
                .filter(item ->
                        item.principalType == principalType && item.credentialName.equalsIgnoreCase(credentialName))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90005",
                        "auth.domain.principal-credential-type.invalid-combination",
                        "Unknown principal credential type: " + principalType + ":" + credentialName));
    }
}
