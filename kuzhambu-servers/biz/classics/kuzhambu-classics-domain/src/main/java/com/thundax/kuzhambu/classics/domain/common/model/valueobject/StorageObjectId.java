package com.thundax.kuzhambu.classics.domain.common.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class StorageObjectId extends BaseLongId {

    private StorageObjectId(Long value) {
        super(value);
    }

    public static StorageObjectId of(Long value) {
        return new StorageObjectId(value);
    }

    public static StorageObjectId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
