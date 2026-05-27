package com.thundax.kuzhambu.system.domain.auth.codec;

import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenId;

public final class PrincipalAccessTokenIdCodec {

    private PrincipalAccessTokenIdCodec() {}

    public static PrincipalAccessTokenId toDomain(String value) {
        return PrincipalAccessTokenId.ofNullable(value);
    }

    public static String toValue(PrincipalAccessTokenId id) {
        return id == null ? null : id.value();
    }

    public static PrincipalAccessTokenId nextId(SnowflakeIdGenerator generator) {
        return toDomain(Long.toHexString(generator.nextId().value()));
    }
}
