package com.thundax.kuzhambu.classics.domain.content.codec;

import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;

public final class ClassicsContentExportJobIdCodec {

    private ClassicsContentExportJobIdCodec() {}

    public static ClassicsContentExportJobId toDomain(Long value) {
        return ClassicsContentExportJobId.ofNullable(value);
    }

    public static Long toValue(ClassicsContentExportJobId id) {
        return id == null ? null : id.value();
    }
}
