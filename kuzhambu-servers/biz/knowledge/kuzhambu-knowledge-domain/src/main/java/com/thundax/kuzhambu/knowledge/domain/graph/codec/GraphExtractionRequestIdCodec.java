package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionRequestId;

public final class GraphExtractionRequestIdCodec {

    private GraphExtractionRequestIdCodec() {}

    public static GraphExtractionRequestId toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new GraphExtractionRequestId(value.trim());
    }

    public static String toValue(GraphExtractionRequestId id) {
        return id == null ? null : id.value();
    }
}
