package com.thundax.kuzhambu.system.domain.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class PrincipalCredentialId extends BaseLongId {

    private PrincipalCredentialId(Long value) {
        super(value);
    }

    public static PrincipalCredentialId of(Long value) {
        return new PrincipalCredentialId(value);
    }

    public static PrincipalCredentialId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
