package com.thundax.kuzhambu.ai.domain.config.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class PromptTemplateId extends BaseLongId {

    private PromptTemplateId(Long value) {
        super(value);
    }

    public static PromptTemplateId of(Long value) {
        return new PromptTemplateId(value);
    }

    public static PromptTemplateId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
