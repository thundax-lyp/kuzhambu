package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionModelId;

public final class GraphExtractionModelIdCodec {

    private GraphExtractionModelIdCodec() {}

    public static GraphExtractionModelId toDomain(Long value) {
        return value == null ? null : new GraphExtractionModelId(value);
    }

    public static Long toValue(GraphExtractionModelId id) {
        return id == null ? null : id.value();
    }
}
