package com.thundax.kuzhambu.ai.domain.invocation.codec;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiPromptVersionId;

public final class AiPromptVersionIdCodec {

    private AiPromptVersionIdCodec() {}

    public static AiPromptVersionId toDomain(Long value) {
        return AiPromptVersionId.ofNullable(value);
    }

    public static AiPromptVersionId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(AiPromptVersionId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(AiPromptVersionId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
