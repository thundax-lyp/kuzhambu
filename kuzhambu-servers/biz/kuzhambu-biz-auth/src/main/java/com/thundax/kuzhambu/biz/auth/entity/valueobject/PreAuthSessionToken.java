package com.thundax.kuzhambu.biz.auth.entity.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PreAuthSessionToken extends BaseStringId {

    private PreAuthSessionToken(String value) {
        super(value);
    }

    public static PreAuthSessionToken of(String value) {
        return new PreAuthSessionToken(value);
    }

    public static PreAuthSessionToken ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
