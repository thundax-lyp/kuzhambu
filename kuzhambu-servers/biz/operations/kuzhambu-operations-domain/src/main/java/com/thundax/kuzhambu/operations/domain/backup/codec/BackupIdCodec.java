package com.thundax.kuzhambu.operations.domain.backup.codec;

import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;

public final class BackupIdCodec {

    private BackupIdCodec() {}

    public static BackupId toDomain(Long value) {
        return BackupId.ofNullable(value);
    }

    public static Long toValue(BackupId id) {
        return id == null ? null : id.value();
    }
}
