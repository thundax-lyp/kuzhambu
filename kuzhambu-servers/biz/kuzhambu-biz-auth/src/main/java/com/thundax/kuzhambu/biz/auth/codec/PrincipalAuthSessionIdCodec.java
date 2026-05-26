package com.thundax.kuzhambu.biz.auth.codec;

import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalAuthSessionId;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;

public final class PrincipalAuthSessionIdCodec {

    private PrincipalAuthSessionIdCodec() {}

    public static PrincipalAuthSessionId toDomain(String value) {
        return PrincipalAuthSessionId.ofNullable(value);
    }

    public static String toValue(PrincipalAuthSessionId id) {
        return id == null ? null : id.value();
    }

    public static PrincipalAuthSessionId nextId(SnowflakeIdGenerator generator) {
        return toDomain(Long.toHexString(generator.nextId().value()));
    }
}
