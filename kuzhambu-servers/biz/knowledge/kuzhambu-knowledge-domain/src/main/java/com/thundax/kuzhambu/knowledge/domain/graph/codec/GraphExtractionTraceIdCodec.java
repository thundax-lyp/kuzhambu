package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTraceId;

public final class GraphExtractionTraceIdCodec {

    private GraphExtractionTraceIdCodec() {}

    public static GraphExtractionTraceId toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new GraphExtractionTraceId(value.trim());
    }

    public static String toValue(GraphExtractionTraceId id) {
        return id == null ? null : id.value();
    }
}
