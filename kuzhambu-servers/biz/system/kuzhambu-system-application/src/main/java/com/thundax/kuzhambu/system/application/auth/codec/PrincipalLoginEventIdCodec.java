package com.thundax.kuzhambu.system.application.auth.codec;

import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalLoginEventId;

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
