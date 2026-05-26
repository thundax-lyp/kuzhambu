package com.thundax.kuzhambu.biz.auth.entity.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PrincipalAuthSessionId extends BaseStringId {

    private PrincipalAuthSessionId(String value) {
        super(value);
    }

    public static PrincipalAuthSessionId of(String value) {
        return new PrincipalAuthSessionId(value);
    }

    public static PrincipalAuthSessionId ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
