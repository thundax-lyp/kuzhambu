package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialVersionId;

public final class GraphMaterialVersionIdCodec {
    private GraphMaterialVersionIdCodec() {}

    public static GraphMaterialVersionId toDomain(Long value) {
        return value == null ? null : new GraphMaterialVersionId(value);
    }

    public static Long toValue(GraphMaterialVersionId value) {
        return value == null ? null : value.value();
    }
}
