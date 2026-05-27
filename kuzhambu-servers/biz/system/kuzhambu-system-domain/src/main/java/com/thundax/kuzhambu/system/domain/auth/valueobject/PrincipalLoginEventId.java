package com.thundax.kuzhambu.system.domain.auth.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PrincipalLoginEventId extends BaseStringId {

    private PrincipalLoginEventId(String value) {
        super(value);
    }

    public static PrincipalLoginEventId of(String value) {
        return new PrincipalLoginEventId(value);
    }

    public static PrincipalLoginEventId ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
