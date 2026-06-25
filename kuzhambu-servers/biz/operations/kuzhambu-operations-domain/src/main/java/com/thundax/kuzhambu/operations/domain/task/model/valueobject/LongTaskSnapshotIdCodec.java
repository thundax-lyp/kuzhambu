package com.thundax.kuzhambu.operations.domain.task.model.valueobject;

public final class LongTaskSnapshotIdCodec {

    private LongTaskSnapshotIdCodec() {}

    public static LongTaskSnapshotId toDomain(Long value) {
        return LongTaskSnapshotId.ofNullable(value);
    }

    public static Long toValue(LongTaskSnapshotId id) {
        return id == null ? null : id.value();
    }
}
