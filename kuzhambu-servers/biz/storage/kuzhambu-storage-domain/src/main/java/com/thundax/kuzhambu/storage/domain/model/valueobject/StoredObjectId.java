package com.thundax.kuzhambu.storage.domain.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class StoredObjectId extends BaseLongId {

    private StoredObjectId(Long value) {
        super(value);
    }

    public static StoredObjectId of(Long value) {
        return new StoredObjectId(value);
    }

    public static StoredObjectId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
