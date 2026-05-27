package com.thundax.kuzhambu.system.domain.auth.codec;

import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalIdentityId;

public final class PrincipalIdentityIdCodec {

    private PrincipalIdentityIdCodec() {}

    public static PrincipalIdentityId toDomain(Long value) {
        return PrincipalIdentityId.ofNullable(value);
    }

    public static Long toValue(PrincipalIdentityId id) {
        return id == null ? null : id.value();
    }
}
