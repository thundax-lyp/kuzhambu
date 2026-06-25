package com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class CleanupJobId extends BaseLongId {

    private CleanupJobId(Long value) {
        super(value);
    }

    public static CleanupJobId of(Long value) {
        return new CleanupJobId(value);
    }

    public static CleanupJobId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
