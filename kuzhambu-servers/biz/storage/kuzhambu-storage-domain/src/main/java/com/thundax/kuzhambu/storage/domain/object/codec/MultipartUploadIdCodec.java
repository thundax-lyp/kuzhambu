package com.thundax.kuzhambu.storage.domain.object.codec;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;

public final class MultipartUploadIdCodec {

    private MultipartUploadIdCodec() {}

    public static MultipartUploadId toDomain(String value) {
        return MultipartUploadId.ofNullable(value);
    }

    public static String toValue(MultipartUploadId id) {
        return id == null ? null : id.value();
    }
}
