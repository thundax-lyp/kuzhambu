package com.thundax.kuzhambu.storage.domain.object.codec;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartSize;

public final class MultipartPartSizeCodec {

    private MultipartPartSizeCodec() {}

    public static MultipartPartSize toDomain(Long value) {
        return value == null ? null : new MultipartPartSize(value);
    }

    public static Long toValue(MultipartPartSize partSize) {
        return partSize == null ? null : partSize.value();
    }
}
