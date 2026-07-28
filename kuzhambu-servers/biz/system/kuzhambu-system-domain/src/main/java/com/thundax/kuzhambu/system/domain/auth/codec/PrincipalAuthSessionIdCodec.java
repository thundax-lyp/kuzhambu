package com.thundax.kuzhambu.system.domain.auth.codec;

import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAuthSessionId;

public final class PrincipalAuthSessionIdCodec {

    private PrincipalAuthSessionIdCodec() {}

    public static PrincipalAuthSessionId toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new PrincipalAuthSessionId(value);
    }

    public static String toValue(PrincipalAuthSessionId id) {
        return id == null ? null : id.value();
    }

    public static PrincipalAuthSessionId nextId(SnowflakeIdGenerator generator) {
        return toDomain(Long.toHexString(generator.nextId().value()));
    }
}
