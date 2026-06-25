package com.thundax.kuzhambu.operations.domain.task.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class LongTaskSnapshotId extends BaseLongId {

    private LongTaskSnapshotId(Long value) {
        super(value);
    }

    public static LongTaskSnapshotId of(Long value) {
        return new LongTaskSnapshotId(value);
    }

    public static LongTaskSnapshotId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
