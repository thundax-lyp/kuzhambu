package com.thundax.kuzhambu.classics.domain.content.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class ClassicsContentId extends BaseLongId {

    private ClassicsContentId(Long value) {
        super(value);
    }

    public static ClassicsContentId of(Long value) {
        return new ClassicsContentId(value);
    }

    public static ClassicsContentId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
