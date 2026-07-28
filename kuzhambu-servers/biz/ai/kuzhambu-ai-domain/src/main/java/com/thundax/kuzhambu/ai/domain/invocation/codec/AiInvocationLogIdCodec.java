package com.thundax.kuzhambu.ai.domain.invocation.codec;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiInvocationLogId;

public final class AiInvocationLogIdCodec {

    private AiInvocationLogIdCodec() {}

    public static AiInvocationLogId toDomain(Long value) {
        return value == null ? null : new AiInvocationLogId(value);
    }

    public static AiInvocationLogId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(AiInvocationLogId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(AiInvocationLogId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
