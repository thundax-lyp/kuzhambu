package com.thundax.kuzhambu.system.domain.auth.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum PrincipalIdentityType {
    USER_ACCOUNT(PrincipalType.USER, "ACCOUNT"),
    USER_MOBILE(PrincipalType.USER, "MOBILE"),
    USER_EMAIL(PrincipalType.USER, "EMAIL"),
    USER_WECOM(PrincipalType.USER, "WECOM"),
    USER_GITHUB(PrincipalType.USER, "GITHUB"),
    MEMBER_ACCOUNT(PrincipalType.MEMBER, "ACCOUNT"),
    MEMBER_MOBILE(PrincipalType.MEMBER, "MOBILE"),
    MEMBER_EMAIL(PrincipalType.MEMBER, "EMAIL"),
    API_KEY(PrincipalType.OPEN_CLIENT, "API_KEY");

    private final PrincipalType principalType;
    private final String identityName;

    PrincipalIdentityType(PrincipalType principalType, String identityName) {
        this.principalType = principalType;
        this.identityName = identityName;
    }

    public String value() {
        return name();
    }

    public PrincipalType principalType() {
        return principalType;
    }

    public String identityName() {
        return identityName;
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

    public boolean isAccount() {
        return "ACCOUNT".equals(identityName);
    }

    public boolean isMobile() {
        return "MOBILE".equals(identityName);
    }

    public boolean isEmail() {
        return "EMAIL".equals(identityName);
    }

    public boolean isApiKey() {
        return "API_KEY".equals(identityName);
    }

    public static PrincipalIdentityType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90007",
                        "auth.domain.principal-identity-type.invalid",
                        "Unknown principal identity type: " + value));
    }

    public static PrincipalIdentityType from(PrincipalType principalType, String identityName) {
        return Arrays.stream(values())
                .filter(item -> item.principalType == principalType && item.identityName.equalsIgnoreCase(identityName))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90008",
                        "auth.domain.principal-identity-type.invalid-combination",
                        "Unknown principal identity type: " + principalType + ":" + identityName));
    }
}
