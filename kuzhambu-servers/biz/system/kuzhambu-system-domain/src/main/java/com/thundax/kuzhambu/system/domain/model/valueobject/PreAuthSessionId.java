package com.thundax.kuzhambu.system.domain.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PreAuthSessionId extends BaseStringId {

    private PreAuthSessionId(String value) {
        super(value);
    }

    public static PreAuthSessionId of(String value) {
        return new PreAuthSessionId(value);
    }

    public static PreAuthSessionId ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
