package com.thundax.kuzhambu.storage.domain.object.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class MultipartUploadSessionId extends BaseLongId {

    private MultipartUploadSessionId(Long value) {
        super(value);
    }

    public static MultipartUploadSessionId of(Long value) {
        return new MultipartUploadSessionId(value);
    }

    public static MultipartUploadSessionId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
