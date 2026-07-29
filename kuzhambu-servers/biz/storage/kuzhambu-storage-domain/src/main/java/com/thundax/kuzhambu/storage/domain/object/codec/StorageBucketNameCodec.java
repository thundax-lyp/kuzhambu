package com.thundax.kuzhambu.storage.domain.object.codec;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageBucketName;

public final class StorageBucketNameCodec {

    private StorageBucketNameCodec() {}

    public static StorageBucketName toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new StorageBucketName(value);
    }

    public static String toValue(StorageBucketName bucketName) {
        return bucketName == null ? null : bucketName.value();
    }
}
