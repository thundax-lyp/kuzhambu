package com.thundax.kuzhambu.operations.domain.task.codec;

import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;

public final class LongTaskSnapshotIdCodec {

    private LongTaskSnapshotIdCodec() {}

    public static LongTaskSnapshotId toDomain(Long value) {
        return value == null ? null : new LongTaskSnapshotId(value);
    }

    public static Long toValue(LongTaskSnapshotId id) {
        return id == null ? null : id.value();
    }
}
