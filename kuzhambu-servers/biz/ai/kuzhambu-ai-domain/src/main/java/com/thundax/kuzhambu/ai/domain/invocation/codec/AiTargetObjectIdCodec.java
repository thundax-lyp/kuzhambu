package com.thundax.kuzhambu.ai.domain.invocation.codec;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;

public final class AiTargetObjectIdCodec {

    private AiTargetObjectIdCodec() {}

    public static AiTargetObjectId toDomain(Long value) {
        return AiTargetObjectId.ofNullable(value);
    }

    public static AiTargetObjectId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(AiTargetObjectId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(AiTargetObjectId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
