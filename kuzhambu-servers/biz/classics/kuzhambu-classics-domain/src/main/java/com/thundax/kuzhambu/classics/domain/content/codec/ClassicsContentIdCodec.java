package com.thundax.kuzhambu.classics.domain.content.codec;

import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;

public final class ClassicsContentIdCodec {

    private ClassicsContentIdCodec() {}

    public static ClassicsContentId toDomain(Long value) {
        return ClassicsContentId.ofNullable(value);
    }

    public static Long toValue(ClassicsContentId id) {
        return id == null ? null : id.value();
    }
}
