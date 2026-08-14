package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphManualSourceId;

public final class GraphManualSourceIdCodec {

    private GraphManualSourceIdCodec() {}

    public static GraphManualSourceId toDomain(Long value) {
        return value == null ? null : new GraphManualSourceId(value);
    }

    public static Long toValue(GraphManualSourceId value) {
        return value == null ? null : value.value();
    }
}
