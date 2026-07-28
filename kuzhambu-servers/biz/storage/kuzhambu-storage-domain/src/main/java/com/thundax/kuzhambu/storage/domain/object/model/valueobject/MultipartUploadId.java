package com.thundax.kuzhambu.storage.domain.object.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class MultipartUploadId extends BaseStringId {

    public MultipartUploadId(String value) {
        super(value == null ? null : value.trim());
    }
}
