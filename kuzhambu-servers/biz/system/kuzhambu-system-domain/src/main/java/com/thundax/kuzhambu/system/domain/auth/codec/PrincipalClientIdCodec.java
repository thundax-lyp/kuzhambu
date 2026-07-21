package com.thundax.kuzhambu.system.domain.auth.codec;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalClientId;

public final class PrincipalClientIdCodec {

    private PrincipalClientIdCodec() {}

    public static PrincipalClientId toDomain(String value) {
        return PrincipalClientId.ofNullable(value);
    }

    public static String toValue(PrincipalClientId id) {
        return id == null ? null : id.value();
    }
}
