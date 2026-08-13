package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;

public final class GraphMaterialNodeIdCodec {
    private GraphMaterialNodeIdCodec() {}

    public static GraphMaterialNodeId toDomain(Long value) {
        return value == null ? null : new GraphMaterialNodeId(value);
    }

    public static Long toValue(GraphMaterialNodeId value) {
        return value == null ? null : value.value();
    }
}
