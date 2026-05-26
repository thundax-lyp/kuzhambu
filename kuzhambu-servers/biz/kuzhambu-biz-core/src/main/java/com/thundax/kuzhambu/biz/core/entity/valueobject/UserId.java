package com.thundax.kuzhambu.biz.core.entity.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class UserId extends BaseLongId {

    private UserId(Long value) {
        super(value);
    }

    public static UserId of(Long value) {
        return new UserId(value);
    }

    public static UserId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
