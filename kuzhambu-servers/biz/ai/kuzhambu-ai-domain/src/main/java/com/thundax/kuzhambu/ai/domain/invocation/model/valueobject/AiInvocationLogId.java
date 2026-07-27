package com.thundax.kuzhambu.ai.domain.invocation.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class AiInvocationLogId extends BaseLongId {

    private AiInvocationLogId(Long value) {
        super(value);
    }

    public static AiInvocationLogId of(Long value) {
        return new AiInvocationLogId(value);
    }

    public static AiInvocationLogId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
