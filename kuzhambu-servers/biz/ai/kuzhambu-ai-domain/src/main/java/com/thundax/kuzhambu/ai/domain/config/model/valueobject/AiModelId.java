package com.thundax.kuzhambu.ai.domain.config.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class AiModelId extends BaseLongId {

    private AiModelId(Long value) {
        super(value);
    }

    public static AiModelId of(Long value) {
        return new AiModelId(value);
    }

    public static AiModelId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
