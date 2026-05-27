package com.thundax.kuzhambu.system.domain.auth.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PrincipalRefreshTokenId extends BaseStringId {

    private PrincipalRefreshTokenId(String value) {
        super(value);
    }

    public static PrincipalRefreshTokenId of(String value) {
        return new PrincipalRefreshTokenId(value);
    }

    public static PrincipalRefreshTokenId ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
