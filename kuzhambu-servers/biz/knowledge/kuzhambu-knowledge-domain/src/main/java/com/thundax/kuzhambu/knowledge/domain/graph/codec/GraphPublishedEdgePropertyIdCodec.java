package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgePropertyId;

public final class GraphPublishedEdgePropertyIdCodec {

    private GraphPublishedEdgePropertyIdCodec() {}

    public static GraphPublishedEdgePropertyId toDomain(Long value) {
        return value == null ? null : new GraphPublishedEdgePropertyId(value);
    }

    public static Long toValue(GraphPublishedEdgePropertyId id) {
        return id == null ? null : id.value();
    }
}
