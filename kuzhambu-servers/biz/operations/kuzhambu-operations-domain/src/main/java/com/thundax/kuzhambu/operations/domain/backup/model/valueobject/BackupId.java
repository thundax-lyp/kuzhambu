package com.thundax.kuzhambu.operations.domain.backup.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class BackupId extends BaseLongId {

    private BackupId(Long value) {
        super(value);
    }

    public static BackupId of(Long value) {
        return new BackupId(value);
    }

    public static BackupId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
