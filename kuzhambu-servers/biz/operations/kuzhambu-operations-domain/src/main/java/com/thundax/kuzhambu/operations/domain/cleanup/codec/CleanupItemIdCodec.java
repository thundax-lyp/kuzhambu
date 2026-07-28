package com.thundax.kuzhambu.operations.domain.cleanup.codec;

import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupItemId;

public final class CleanupItemIdCodec {

    private CleanupItemIdCodec() {}

    public static CleanupItemId toDomain(Long value) {
        return value == null ? null : new CleanupItemId(value);
    }

    public static Long toValue(CleanupItemId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(CleanupItemId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
