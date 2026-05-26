package com.thundax.kuzhambu.biz.auth.codec;

import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalLoginEventId;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;

public final class PrincipalLoginEventIdCodec {

    private PrincipalLoginEventIdCodec() {}

    public static PrincipalLoginEventId toDomain(String value) {
        return PrincipalLoginEventId.ofNullable(value);
    }

    public static String toValue(PrincipalLoginEventId id) {
        return id == null ? null : id.value();
    }

    public static PrincipalLoginEventId nextId(SnowflakeIdGenerator generator) {
        return toDomain(Long.toHexString(generator.nextId().value()));
    }
}
