package com.thundax.kuzhambu.ai.domain.config.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class PromptVersionId extends BaseLongId {

    private PromptVersionId(Long value) {
        super(value);
    }

    public static PromptVersionId of(Long value) {
        return new PromptVersionId(value);
    }

    public static PromptVersionId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
