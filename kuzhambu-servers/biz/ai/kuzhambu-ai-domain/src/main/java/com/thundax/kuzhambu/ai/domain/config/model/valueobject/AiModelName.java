package com.thundax.kuzhambu.ai.domain.config.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class AiModelName extends BaseStringId {

    private AiModelName(String value) {
        super(value);
    }

    public static AiModelName of(String value) {
        return new AiModelName(value.trim());
    }

    public static AiModelName ofNullable(String value) {
        return value == null || value.trim().isEmpty() ? null : of(value);
    }
}
