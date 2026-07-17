package com.thundax.kuzhambu.ai.domain.config.codec;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import java.util.List;
import java.util.stream.Collectors;

public final class PromptVersionIdCodec {

    private PromptVersionIdCodec() {}

    public static PromptVersionId toDomain(Long value) {
        return PromptVersionId.ofNullable(value);
    }

    public static PromptVersionId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(PromptVersionId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(PromptVersionId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<PromptVersionId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(PromptVersionIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<PromptVersionId> ids) {
        return ids == null
                ? null
                : ids.stream().map(PromptVersionIdCodec::toValue).collect(Collectors.toList());
    }
}
