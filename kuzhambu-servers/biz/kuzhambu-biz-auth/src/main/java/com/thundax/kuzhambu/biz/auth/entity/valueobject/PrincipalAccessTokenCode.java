package com.thundax.kuzhambu.biz.auth.entity.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PrincipalAccessTokenCode extends BaseStringId {

    private PrincipalAccessTokenCode(String value) {
        super(value);
    }

    public static PrincipalAccessTokenCode of(String value) {
        return new PrincipalAccessTokenCode(value);
    }

    public static PrincipalAccessTokenCode ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
