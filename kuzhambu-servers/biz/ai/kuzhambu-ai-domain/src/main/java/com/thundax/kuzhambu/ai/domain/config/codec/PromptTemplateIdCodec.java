package com.thundax.kuzhambu.ai.domain.config.codec;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import java.util.List;
import java.util.stream.Collectors;

public final class PromptTemplateIdCodec {

    private PromptTemplateIdCodec() {}

    public static PromptTemplateId toDomain(Long value) {
        return PromptTemplateId.ofNullable(value);
    }

    public static PromptTemplateId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(PromptTemplateId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(PromptTemplateId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<PromptTemplateId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(PromptTemplateIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<PromptTemplateId> ids) {
        return ids == null
                ? null
                : ids.stream().map(PromptTemplateIdCodec::toValue).collect(Collectors.toList());
    }
}
