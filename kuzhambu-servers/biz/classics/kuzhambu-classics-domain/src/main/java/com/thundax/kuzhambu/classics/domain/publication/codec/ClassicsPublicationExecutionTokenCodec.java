package com.thundax.kuzhambu.classics.domain.publication.codec;

import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;

public final class ClassicsPublicationExecutionTokenCodec {

    private ClassicsPublicationExecutionTokenCodec() {}

    public static ClassicsPublicationExecutionToken toDomain(String value) {
        return value == null ? null : new ClassicsPublicationExecutionToken(value);
    }

    public static String toValue(ClassicsPublicationExecutionToken token) {
        return token == null ? null : token.value();
    }
}
