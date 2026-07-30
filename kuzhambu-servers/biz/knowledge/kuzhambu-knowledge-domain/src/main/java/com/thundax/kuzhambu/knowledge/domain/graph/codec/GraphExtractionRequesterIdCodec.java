package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionRequesterId;

public final class GraphExtractionRequesterIdCodec {

    private GraphExtractionRequesterIdCodec() {}

    public static GraphExtractionRequesterId toDomain(Long value) {
        return value == null ? null : new GraphExtractionRequesterId(value);
    }

    public static Long toValue(GraphExtractionRequesterId id) {
        return id == null ? null : id.value();
    }
}
