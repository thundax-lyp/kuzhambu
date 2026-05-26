package com.thundax.kuzhambu.biz.auth.codec;

import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalAccessTokenId;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;

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
