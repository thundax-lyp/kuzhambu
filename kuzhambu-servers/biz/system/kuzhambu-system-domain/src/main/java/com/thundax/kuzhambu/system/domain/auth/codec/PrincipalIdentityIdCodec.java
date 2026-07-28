package com.thundax.kuzhambu.system.domain.auth.codec;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;

public final class PrincipalIdentityIdCodec {

    private PrincipalIdentityIdCodec() {}

    public static PrincipalIdentityId toDomain(Long value) {
        return value == null ? null : new PrincipalIdentityId(value);
    }

    public static Long toValue(PrincipalIdentityId id) {
        return id == null ? null : id.value();
    }
}
