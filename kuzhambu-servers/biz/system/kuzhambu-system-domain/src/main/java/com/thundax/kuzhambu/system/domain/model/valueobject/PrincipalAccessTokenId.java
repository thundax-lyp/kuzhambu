package com.thundax.kuzhambu.system.domain.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PrincipalAccessTokenId extends BaseStringId {

    private PrincipalAccessTokenId(String value) {
        super(value);
    }

    public static PrincipalAccessTokenId of(String value) {
        return new PrincipalAccessTokenId(value);
    }

    public static PrincipalAccessTokenId ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
