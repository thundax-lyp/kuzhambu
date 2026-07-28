package com.thundax.kuzhambu.system.domain.auth.codec;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalCredentialId;

public final class PrincipalCredentialIdCodec {

    private PrincipalCredentialIdCodec() {}

    public static PrincipalCredentialId toDomain(Long value) {
        return value == null ? null : new PrincipalCredentialId(value);
    }

    public static Long toValue(PrincipalCredentialId id) {
        return id == null ? null : id.value();
    }
}
