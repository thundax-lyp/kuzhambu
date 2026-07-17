package com.thundax.kuzhambu.ai.domain.config.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class PromptVariableId extends BaseLongId {

    private PromptVariableId(Long value) {
        super(value);
    }

    public static PromptVariableId of(Long value) {
        return new PromptVariableId(value);
    }

    public static PromptVariableId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
