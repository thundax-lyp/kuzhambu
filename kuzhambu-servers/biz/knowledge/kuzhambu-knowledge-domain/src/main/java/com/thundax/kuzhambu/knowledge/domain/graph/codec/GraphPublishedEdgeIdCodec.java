package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;

public final class GraphPublishedEdgeIdCodec {
    private GraphPublishedEdgeIdCodec() {}

    public static GraphPublishedEdgeId toDomain(Long value) {
        return value == null ? null : new GraphPublishedEdgeId(value);
    }

    public static Long toValue(GraphPublishedEdgeId value) {
        return value == null ? null : value.value();
    }
}
