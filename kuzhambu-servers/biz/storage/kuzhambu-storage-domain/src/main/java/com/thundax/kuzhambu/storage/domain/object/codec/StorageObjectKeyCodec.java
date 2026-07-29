package com.thundax.kuzhambu.storage.domain.object.codec;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageObjectKey;

public final class StorageObjectKeyCodec {

    private StorageObjectKeyCodec() {}

    public static StorageObjectKey toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new StorageObjectKey(value);
    }

    public static String toValue(StorageObjectKey objectKey) {
        return objectKey == null ? null : objectKey.value();
    }
}
