package com.thundax.kuzhambu.system.domain.core.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class MenuId extends BaseLongId {

    private MenuId(Long value) {
        super(value);
    }

    public static MenuId of(Long value) {
        return new MenuId(value);
    }

    public static MenuId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
