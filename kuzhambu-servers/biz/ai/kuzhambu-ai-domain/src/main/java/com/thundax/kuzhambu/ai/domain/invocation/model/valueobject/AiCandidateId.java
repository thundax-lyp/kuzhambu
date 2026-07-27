package com.thundax.kuzhambu.ai.domain.invocation.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class AiCandidateId extends BaseLongId {

    private AiCandidateId(Long value) {
        super(value);
    }

    public static AiCandidateId of(Long value) {
        return new AiCandidateId(value);
    }

    public static AiCandidateId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
