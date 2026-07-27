package com.thundax.kuzhambu.ai.domain.batch.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class AiBatchJobId extends BaseLongId {

    private AiBatchJobId(Long value) {
        super(value);
    }

    public static AiBatchJobId of(Long value) {
        return new AiBatchJobId(value);
    }

    public static AiBatchJobId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
