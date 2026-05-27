package com.thundax.kuzhambu.system.application.auth.entity.valueobject;

public final class PrincipalCredentialIdCodec {

    private PrincipalCredentialIdCodec() {}

    public static PrincipalCredentialId toDomain(Long value) {
        return PrincipalCredentialId.ofNullable(value);
    }

    public static Long toValue(PrincipalCredentialId id) {
        return id == null ? null : id.value();
    }
}
