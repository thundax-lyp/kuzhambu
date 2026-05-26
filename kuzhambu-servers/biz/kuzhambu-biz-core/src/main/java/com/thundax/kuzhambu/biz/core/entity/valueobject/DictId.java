package com.thundax.kuzhambu.biz.core.entity.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class DictId extends BaseLongId {

    private DictId(Long value) {
        super(value);
    }

    public static DictId of(Long value) {
        return new DictId(value);
    }

    public static DictId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
