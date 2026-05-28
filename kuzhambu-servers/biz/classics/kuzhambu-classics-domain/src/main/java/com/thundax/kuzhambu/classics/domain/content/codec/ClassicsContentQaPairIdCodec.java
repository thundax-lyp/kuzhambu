package com.thundax.kuzhambu.classics.domain.content.codec;

import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;

public final class ClassicsContentQaPairIdCodec {

    private ClassicsContentQaPairIdCodec() {}

    public static ClassicsContentQaPairId toDomain(Long value) {
        return ClassicsContentQaPairId.ofNullable(value);
    }

    public static Long toValue(ClassicsContentQaPairId id) {
        return id == null ? null : id.value();
    }
}
