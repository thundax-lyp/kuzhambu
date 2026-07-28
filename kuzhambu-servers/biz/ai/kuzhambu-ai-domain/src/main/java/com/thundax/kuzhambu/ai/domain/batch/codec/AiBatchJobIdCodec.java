package com.thundax.kuzhambu.ai.domain.batch.codec;

import com.thundax.kuzhambu.ai.domain.batch.model.valueobject.AiBatchJobId;

public final class AiBatchJobIdCodec {

    private AiBatchJobIdCodec() {}

    public static AiBatchJobId toDomain(Long value) {
        return value == null ? null : new AiBatchJobId(value);
    }

    public static AiBatchJobId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(AiBatchJobId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(AiBatchJobId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
