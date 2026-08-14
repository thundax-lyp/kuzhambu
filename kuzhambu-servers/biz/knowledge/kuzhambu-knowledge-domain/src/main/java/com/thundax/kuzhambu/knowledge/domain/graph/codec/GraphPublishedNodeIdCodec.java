package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;

public final class GraphPublishedNodeIdCodec {
    private GraphPublishedNodeIdCodec() {}

    public static GraphPublishedNodeId toDomain(Long value) {
        return value == null ? null : new GraphPublishedNodeId(value);
    }

    public static Long toValue(GraphPublishedNodeId value) {
        return value == null ? null : value.value();
    }
}
