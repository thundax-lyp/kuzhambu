package com.thundax.kuzhambu.ai.domain.invocation.codec;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;

public final class AiCallIdCodec {

    private AiCallIdCodec() {}

    public static AiCallId toDomain(Long value) {
        return AiCallId.ofNullable(value);
    }

    public static AiCallId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(AiCallId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(AiCallId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
