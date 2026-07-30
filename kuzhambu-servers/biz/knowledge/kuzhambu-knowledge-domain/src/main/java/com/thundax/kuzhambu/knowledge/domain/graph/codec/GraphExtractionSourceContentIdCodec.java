package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionSourceContentId;

public final class GraphExtractionSourceContentIdCodec {

    private GraphExtractionSourceContentIdCodec() {}

    public static GraphExtractionSourceContentId toDomain(Long value) {
        return value == null ? null : new GraphExtractionSourceContentId(value);
    }

    public static Long toValue(GraphExtractionSourceContentId id) {
        return id == null ? null : id.value();
    }
}
