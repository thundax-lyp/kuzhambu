package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;

public final class GraphMaterialEdgeIdCodec {
    private GraphMaterialEdgeIdCodec() {}

    public static GraphMaterialEdgeId toDomain(Long value) {
        return value == null ? null : new GraphMaterialEdgeId(value);
    }

    public static Long toValue(GraphMaterialEdgeId value) {
        return value == null ? null : value.value();
    }
}
