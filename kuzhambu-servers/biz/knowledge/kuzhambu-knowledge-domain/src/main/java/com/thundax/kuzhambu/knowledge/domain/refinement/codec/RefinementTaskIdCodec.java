package com.thundax.kuzhambu.knowledge.domain.refinement.codec;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.valueobject.RefinementTaskId;

public final class RefinementTaskIdCodec {

    private RefinementTaskIdCodec() {}

    public static RefinementTaskId toDomain(Long value) {
        return value == null ? null : new RefinementTaskId(value);
    }

    public static Long toValue(RefinementTaskId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(RefinementTaskId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
