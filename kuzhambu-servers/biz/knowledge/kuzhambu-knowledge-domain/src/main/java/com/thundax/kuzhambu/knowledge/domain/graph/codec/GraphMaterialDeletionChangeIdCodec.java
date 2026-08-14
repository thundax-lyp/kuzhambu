package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionChangeId;

public final class GraphMaterialDeletionChangeIdCodec {
    private GraphMaterialDeletionChangeIdCodec() {}

    public static GraphMaterialDeletionChangeId toDomain(Long value) {
        return value == null ? null : new GraphMaterialDeletionChangeId(value);
    }

    public static Long toValue(GraphMaterialDeletionChangeId value) {
        return value == null ? null : value.value();
    }
}
