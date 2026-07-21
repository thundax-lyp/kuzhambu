package com.thundax.kuzhambu.system.domain.auth.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PrincipalClientId extends BaseStringId {

    private PrincipalClientId(String value) {
        super(value);
    }

    public static PrincipalClientId of(String value) {
        return new PrincipalClientId(value);
    }

    public static PrincipalClientId ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
