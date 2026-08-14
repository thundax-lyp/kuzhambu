package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodePropertyId;

public final class GraphPublishedNodePropertyIdCodec {

    private GraphPublishedNodePropertyIdCodec() {}

    public static GraphPublishedNodePropertyId toDomain(Long value) {
        return value == null ? null : new GraphPublishedNodePropertyId(value);
    }

    public static Long toValue(GraphPublishedNodePropertyId id) {
        return id == null ? null : id.value();
    }
}
