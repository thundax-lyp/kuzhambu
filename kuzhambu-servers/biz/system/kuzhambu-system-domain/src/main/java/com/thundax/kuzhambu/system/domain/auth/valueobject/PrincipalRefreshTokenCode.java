package com.thundax.kuzhambu.system.domain.auth.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PrincipalRefreshTokenCode extends BaseStringId {

    private PrincipalRefreshTokenCode(String value) {
        super(value);
    }

    public static PrincipalRefreshTokenCode of(String value) {
        return new PrincipalRefreshTokenCode(value);
    }

    public static PrincipalRefreshTokenCode ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
