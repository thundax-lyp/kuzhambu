package com.thundax.kuzhambu.knowledge.domain.refinement.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class RefinementTaskId extends BaseLongId {

    private RefinementTaskId(Long value) {
        super(value);
    }

    public static RefinementTaskId of(Long value) {
        return new RefinementTaskId(value);
    }

    public static RefinementTaskId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
