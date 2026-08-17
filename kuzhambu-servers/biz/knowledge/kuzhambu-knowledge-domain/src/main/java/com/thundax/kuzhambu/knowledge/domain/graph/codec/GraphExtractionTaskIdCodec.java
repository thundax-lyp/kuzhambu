package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;

public final class GraphExtractionTaskIdCodec {
    private GraphExtractionTaskIdCodec() {}

    public static GraphExtractionTaskId toDomain(Long value) {
        return value == null ? null : new GraphExtractionTaskId(value);
    }

    public static Long toValue(GraphExtractionTaskId value) {
        return value == null ? null : value.value();
    }
}
