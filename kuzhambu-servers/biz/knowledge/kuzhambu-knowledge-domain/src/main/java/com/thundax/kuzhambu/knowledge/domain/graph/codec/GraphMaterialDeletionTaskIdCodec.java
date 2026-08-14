package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionTaskId;

public final class GraphMaterialDeletionTaskIdCodec {
    private GraphMaterialDeletionTaskIdCodec() {}

    public static GraphMaterialDeletionTaskId toDomain(Long value) {
        return value == null ? null : new GraphMaterialDeletionTaskId(value);
    }

    public static Long toValue(GraphMaterialDeletionTaskId value) {
        return value == null ? null : value.value();
    }
}
