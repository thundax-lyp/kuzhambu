package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionBatchJobId;

public final class GraphExtractionBatchJobIdCodec {

    private GraphExtractionBatchJobIdCodec() {}

    public static GraphExtractionBatchJobId toDomain(Long value) {
        return value == null ? null : new GraphExtractionBatchJobId(value);
    }

    public static Long toValue(GraphExtractionBatchJobId id) {
        return id == null ? null : id.value();
    }
}
