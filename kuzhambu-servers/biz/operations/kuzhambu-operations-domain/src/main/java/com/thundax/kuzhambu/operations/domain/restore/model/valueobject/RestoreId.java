package com.thundax.kuzhambu.operations.domain.restore.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class RestoreId extends BaseLongId {

    private RestoreId(Long value) {
        super(value);
    }

    public static RestoreId of(Long value) {
        return new RestoreId(value);
    }

    public static RestoreId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
