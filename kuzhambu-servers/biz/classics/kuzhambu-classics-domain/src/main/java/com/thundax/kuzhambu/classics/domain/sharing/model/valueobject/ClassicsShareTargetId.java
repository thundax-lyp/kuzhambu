package com.thundax.kuzhambu.classics.domain.sharing.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class ClassicsShareTargetId extends BaseLongId {

    private ClassicsShareTargetId(Long value) {
        super(value);
    }

    public static ClassicsShareTargetId of(Long value) {
        return new ClassicsShareTargetId(value);
    }

    public static ClassicsShareTargetId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
