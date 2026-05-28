package com.thundax.kuzhambu.classics.domain.sharing.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class ClassicsShareLinkId extends BaseLongId {

    private ClassicsShareLinkId(Long value) {
        super(value);
    }

    public static ClassicsShareLinkId of(Long value) {
        return new ClassicsShareLinkId(value);
    }

    public static ClassicsShareLinkId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
