package com.thundax.kuzhambu.storage.domain.object.codec;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageReferenceOwnerType;

public final class StorageReferenceOwnerTypeCodec {

    private StorageReferenceOwnerTypeCodec() {}

    public static StorageReferenceOwnerType toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new StorageReferenceOwnerType(value);
    }

    public static String toValue(StorageReferenceOwnerType referenceOwnerType) {
        return referenceOwnerType == null ? null : referenceOwnerType.value();
    }
}
