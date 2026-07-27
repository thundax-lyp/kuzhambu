package com.thundax.kuzhambu.ai.domain.invocation.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class AiCallId extends BaseLongId {

    private AiCallId(Long value) {
        super(value);
    }

    public static AiCallId of(Long value) {
        return new AiCallId(value);
    }

    public static AiCallId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
