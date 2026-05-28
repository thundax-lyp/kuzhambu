package com.thundax.kuzhambu.classics.domain.content.codec;

import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;

public final class ClassicsContentVersionIdCodec {

    private ClassicsContentVersionIdCodec() {}

    public static ClassicsContentVersionId toDomain(Long value) {
        return ClassicsContentVersionId.ofNullable(value);
    }

    public static Long toValue(ClassicsContentVersionId id) {
        return id == null ? null : id.value();
    }
}
