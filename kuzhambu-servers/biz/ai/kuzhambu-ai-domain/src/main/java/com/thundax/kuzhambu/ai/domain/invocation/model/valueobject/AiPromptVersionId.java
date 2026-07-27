package com.thundax.kuzhambu.ai.domain.invocation.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class AiPromptVersionId extends BaseLongId {

    private AiPromptVersionId(Long value) {
        super(value);
    }

    public static AiPromptVersionId of(Long value) {
        return new AiPromptVersionId(value);
    }

    public static AiPromptVersionId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
