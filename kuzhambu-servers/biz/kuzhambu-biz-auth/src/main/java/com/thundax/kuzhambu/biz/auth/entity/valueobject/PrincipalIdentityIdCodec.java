package com.thundax.kuzhambu.biz.auth.entity.valueobject;

public final class PrincipalIdentityIdCodec {

    private PrincipalIdentityIdCodec() {}

    public static PrincipalIdentityId toDomain(Long value) {
        return PrincipalIdentityId.ofNullable(value);
    }

    public static Long toValue(PrincipalIdentityId id) {
        return id == null ? null : id.value();
    }
}
