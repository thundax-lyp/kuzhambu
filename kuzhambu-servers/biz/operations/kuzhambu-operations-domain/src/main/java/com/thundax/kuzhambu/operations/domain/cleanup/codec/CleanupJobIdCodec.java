package com.thundax.kuzhambu.operations.domain.cleanup.codec;

import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;

public final class CleanupJobIdCodec {

    private CleanupJobIdCodec() {}

    public static CleanupJobId toDomain(Long value) {
        return value == null ? null : new CleanupJobId(value);
    }

    public static Long toValue(CleanupJobId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(CleanupJobId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
