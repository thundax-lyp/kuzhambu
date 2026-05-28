package com.thundax.kuzhambu.classics.domain.sharing.codec;

import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareAccessRecordId;

public final class ClassicsShareAccessRecordIdCodec {

    private ClassicsShareAccessRecordIdCodec() {}

    public static ClassicsShareAccessRecordId toDomain(Long value) {
        return ClassicsShareAccessRecordId.ofNullable(value);
    }

    public static Long toValue(ClassicsShareAccessRecordId id) {
        return id == null ? null : id.value();
    }
}
