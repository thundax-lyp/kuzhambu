package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;

public final class GraphVersionIdCodec {

    private GraphVersionIdCodec() {}

    public static GraphVersionId toDomain(Long value) {
        return value == null ? null : new GraphVersionId(value);
    }

    public static Long toValue(GraphVersionId id) {
        return id == null ? null : id.value();
    }
}
