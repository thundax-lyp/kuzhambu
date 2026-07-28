package com.thundax.kuzhambu.classics.domain.content.codec;

import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;

public final class ClassicsContentTagIdCodec {

    private ClassicsContentTagIdCodec() {}

    public static ClassicsContentTagId toDomain(Long value) {
        return value == null ? null : new ClassicsContentTagId(value);
    }

    public static Long toValue(ClassicsContentTagId id) {
        return id == null ? null : id.value();
    }
}
