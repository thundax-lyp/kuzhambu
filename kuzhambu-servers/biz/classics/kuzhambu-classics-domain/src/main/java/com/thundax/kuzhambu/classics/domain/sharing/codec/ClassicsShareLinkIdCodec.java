package com.thundax.kuzhambu.classics.domain.sharing.codec;

import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;

public final class ClassicsShareLinkIdCodec {

    private ClassicsShareLinkIdCodec() {}

    public static ClassicsShareLinkId toDomain(Long value) {
        return ClassicsShareLinkId.ofNullable(value);
    }

    public static Long toValue(ClassicsShareLinkId id) {
        return id == null ? null : id.value();
    }
}
