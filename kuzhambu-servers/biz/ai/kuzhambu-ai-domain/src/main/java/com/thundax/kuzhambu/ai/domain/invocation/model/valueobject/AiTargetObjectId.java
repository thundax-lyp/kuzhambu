package com.thundax.kuzhambu.ai.domain.invocation.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class AiTargetObjectId extends BaseLongId {

    private AiTargetObjectId(Long value) {
        super(value);
    }

    public static AiTargetObjectId of(Long value) {
        return new AiTargetObjectId(value);
    }

    public static AiTargetObjectId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
