package com.thundax.kuzhambu.operations.domain.restore.codec;

import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;

public final class RestoreIdCodec {

    private RestoreIdCodec() {}

    public static RestoreId toDomain(Long value) {
        return RestoreId.ofNullable(value);
    }

    public static Long toValue(RestoreId id) {
        return id == null ? null : id.value();
    }
}
