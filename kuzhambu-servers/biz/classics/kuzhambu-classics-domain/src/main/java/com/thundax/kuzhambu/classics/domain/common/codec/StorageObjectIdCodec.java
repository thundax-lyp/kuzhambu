package com.thundax.kuzhambu.classics.domain.common.codec;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;

public final class StorageObjectIdCodec {

    private StorageObjectIdCodec() {}

    public static StorageObjectId toDomain(Long value) {
        return value == null ? null : new StorageObjectId(value);
    }

    public static Long toValue(StorageObjectId id) {
        return id == null ? null : id.value();
    }
}
