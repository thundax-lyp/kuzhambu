package com.thundax.kuzhambu.storage.domain.object.codec;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartNumber;

public final class MultipartPartNumberCodec {

    private MultipartPartNumberCodec() {}

    public static MultipartPartNumber toDomain(Integer value) {
        return value == null ? null : new MultipartPartNumber(value);
    }

    public static Integer toValue(MultipartPartNumber partNumber) {
        return partNumber == null ? null : partNumber.value();
    }
}
