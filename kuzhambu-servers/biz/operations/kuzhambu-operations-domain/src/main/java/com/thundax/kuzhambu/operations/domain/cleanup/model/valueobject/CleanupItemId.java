package com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class CleanupItemId extends BaseLongId {

    private CleanupItemId(Long value) {
        super(value);
    }

    public static CleanupItemId of(Long value) {
        return new CleanupItemId(value);
    }

    public static CleanupItemId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
