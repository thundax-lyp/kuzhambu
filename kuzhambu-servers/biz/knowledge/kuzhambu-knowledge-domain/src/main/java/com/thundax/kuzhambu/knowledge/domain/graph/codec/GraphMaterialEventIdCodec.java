package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;

public final class GraphMaterialEventIdCodec {
    private GraphMaterialEventIdCodec() {}

    public static GraphMaterialEventId toDomain(Long value) {
        return value == null ? null : new GraphMaterialEventId(value);
    }

    public static Long toValue(GraphMaterialEventId value) {
        return value == null ? null : value.value();
    }
}
