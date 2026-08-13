package com.thundax.kuzhambu.knowledge.domain.graph.material.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.material.model.valueobject.GraphMaterialId;

public final class GraphMaterialIdCodec {

    private GraphMaterialIdCodec() {}

    public static GraphMaterialId toDomain(Long value) {
        return value == null ? null : new GraphMaterialId(value);
    }

    public static Long toValue(GraphMaterialId id) {
        return id == null ? null : id.value();
    }
}
