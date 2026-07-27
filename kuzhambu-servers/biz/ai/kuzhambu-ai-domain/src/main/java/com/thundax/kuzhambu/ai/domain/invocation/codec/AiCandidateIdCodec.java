package com.thundax.kuzhambu.ai.domain.invocation.codec;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;

public final class AiCandidateIdCodec {

    private AiCandidateIdCodec() {}

    public static AiCandidateId toDomain(Long value) {
        return AiCandidateId.ofNullable(value);
    }

    public static AiCandidateId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(AiCandidateId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(AiCandidateId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
