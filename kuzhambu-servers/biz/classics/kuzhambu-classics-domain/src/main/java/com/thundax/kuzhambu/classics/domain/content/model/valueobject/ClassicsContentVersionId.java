package com.thundax.kuzhambu.classics.domain.content.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class ClassicsContentVersionId extends BaseLongId {

    private ClassicsContentVersionId(Long value) {
        super(value);
    }

    public static ClassicsContentVersionId of(Long value) {
        return new ClassicsContentVersionId(value);
    }

    public static ClassicsContentVersionId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
