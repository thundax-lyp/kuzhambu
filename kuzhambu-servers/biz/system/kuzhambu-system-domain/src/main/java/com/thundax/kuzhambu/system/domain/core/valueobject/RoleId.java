package com.thundax.kuzhambu.system.domain.core.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class RoleId extends BaseLongId {

    private RoleId(Long value) {
        super(value);
    }

    public static RoleId of(Long value) {
        return new RoleId(value);
    }

    public static RoleId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
