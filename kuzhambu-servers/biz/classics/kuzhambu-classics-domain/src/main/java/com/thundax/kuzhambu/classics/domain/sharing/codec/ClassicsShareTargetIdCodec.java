package com.thundax.kuzhambu.classics.domain.sharing.codec;

import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;

public final class ClassicsShareTargetIdCodec {

    private ClassicsShareTargetIdCodec() {}

    public static ClassicsShareTargetId toDomain(Long value) {
        return ClassicsShareTargetId.ofNullable(value);
    }

    public static Long toValue(ClassicsShareTargetId id) {
        return id == null ? null : id.value();
    }
}
