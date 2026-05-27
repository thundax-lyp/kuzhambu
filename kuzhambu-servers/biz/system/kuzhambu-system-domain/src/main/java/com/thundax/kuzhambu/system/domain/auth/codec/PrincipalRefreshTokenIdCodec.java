package com.thundax.kuzhambu.system.domain.auth.codec;

import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalRefreshTokenId;

public final class PrincipalRefreshTokenIdCodec {

    private PrincipalRefreshTokenIdCodec() {}

    public static PrincipalRefreshTokenId toDomain(String value) {
        return PrincipalRefreshTokenId.ofNullable(value);
    }

    public static String toValue(PrincipalRefreshTokenId id) {
        return id == null ? null : id.value();
    }

    public static PrincipalRefreshTokenId nextId(SnowflakeIdGenerator generator) {
        return toDomain(Long.toHexString(generator.nextId().value()));
    }
}
