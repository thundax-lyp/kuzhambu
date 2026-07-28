package com.thundax.kuzhambu.system.domain.auth.codec;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;

public final class PreAuthSessionIdCodec {

    private PreAuthSessionIdCodec() {}

    public static PreAuthSessionId toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new PreAuthSessionId(value);
    }

    public static String toValue(PreAuthSessionId id) {
        return id == null ? null : id.value();
    }
}
