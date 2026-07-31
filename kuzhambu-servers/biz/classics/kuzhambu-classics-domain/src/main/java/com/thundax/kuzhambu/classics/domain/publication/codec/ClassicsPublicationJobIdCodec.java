package com.thundax.kuzhambu.classics.domain.publication.codec;

import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;

public final class ClassicsPublicationJobIdCodec {

    private ClassicsPublicationJobIdCodec() {}

    public static ClassicsPublicationJobId toDomain(Long value) {
        return value == null ? null : new ClassicsPublicationJobId(value);
    }

    public static Long toValue(ClassicsPublicationJobId id) {
        return id == null ? null : id.value();
    }
}
