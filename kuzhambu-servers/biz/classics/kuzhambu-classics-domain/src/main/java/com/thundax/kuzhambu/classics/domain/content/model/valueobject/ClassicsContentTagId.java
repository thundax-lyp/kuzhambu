package com.thundax.kuzhambu.classics.domain.content.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class ClassicsContentTagId extends BaseLongId {

    private ClassicsContentTagId(Long value) {
        super(value);
    }

    public static ClassicsContentTagId of(Long value) {
        return new ClassicsContentTagId(value);
    }

    public static ClassicsContentTagId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
