package com.thundax.kuzhambu.classics.domain.content.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class ClassicsContentExportJobId extends BaseLongId {

    private ClassicsContentExportJobId(Long value) {
        super(value);
    }

    public static ClassicsContentExportJobId of(Long value) {
        return new ClassicsContentExportJobId(value);
    }

    public static ClassicsContentExportJobId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
