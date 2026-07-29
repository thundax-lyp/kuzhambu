package com.thundax.kuzhambu.storage.domain.object.codec;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;

public final class StorageMimeTypeCodec {

    private StorageMimeTypeCodec() {}

    public static StorageMimeType toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new StorageMimeType(value);
    }

    public static String toValue(StorageMimeType mimeType) {
        return mimeType == null ? null : mimeType.value();
    }
}
