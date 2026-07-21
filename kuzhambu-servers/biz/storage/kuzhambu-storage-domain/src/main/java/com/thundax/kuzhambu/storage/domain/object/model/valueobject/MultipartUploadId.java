package com.thundax.kuzhambu.storage.domain.object.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class MultipartUploadId extends BaseStringId {

    private MultipartUploadId(String value) {
        super(value);
    }

    public static MultipartUploadId of(String value) {
        return new MultipartUploadId(value.trim());
    }

    public static MultipartUploadId ofNullable(String value) {
        return value == null || value.trim().isEmpty() ? null : of(value);
    }
}
