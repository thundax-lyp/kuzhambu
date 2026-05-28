package com.thundax.kuzhambu.classics.domain.sharing.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class ClassicsShareAccessRecordId extends BaseLongId {

    private ClassicsShareAccessRecordId(Long value) {
        super(value);
    }

    public static ClassicsShareAccessRecordId of(Long value) {
        return new ClassicsShareAccessRecordId(value);
    }

    public static ClassicsShareAccessRecordId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
