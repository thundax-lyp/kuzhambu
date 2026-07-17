package com.thundax.kuzhambu.ai.domain.config.codec;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVariableId;
import java.util.List;
import java.util.stream.Collectors;

public final class PromptVariableIdCodec {

    private PromptVariableIdCodec() {}

    public static PromptVariableId toDomain(Long value) {
        return PromptVariableId.ofNullable(value);
    }

    public static PromptVariableId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(PromptVariableId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(PromptVariableId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<PromptVariableId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(PromptVariableIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<PromptVariableId> ids) {
        return ids == null
                ? null
                : ids.stream().map(PromptVariableIdCodec::toValue).collect(Collectors.toList());
    }
}
