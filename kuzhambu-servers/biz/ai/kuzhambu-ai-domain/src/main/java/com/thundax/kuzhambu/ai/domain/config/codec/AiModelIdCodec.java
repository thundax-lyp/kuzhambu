package com.thundax.kuzhambu.ai.domain.config.codec;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import java.util.List;
import java.util.stream.Collectors;

public final class AiModelIdCodec {

    private AiModelIdCodec() {}

    public static AiModelId toDomain(Long value) {
        return value == null ? null : new AiModelId(value);
    }

    public static AiModelId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(AiModelId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(AiModelId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<AiModelId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(AiModelIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<AiModelId> ids) {
        return ids == null ? null : ids.stream().map(AiModelIdCodec::toValue).collect(Collectors.toList());
    }
}
