package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionAiCallId;

public final class GraphExtractionAiCallIdCodec {

    private GraphExtractionAiCallIdCodec() {}

    public static GraphExtractionAiCallId toDomain(Long value) {
        return value == null ? null : new GraphExtractionAiCallId(value);
    }

    public static Long toValue(GraphExtractionAiCallId id) {
        return id == null ? null : id.value();
    }
}
