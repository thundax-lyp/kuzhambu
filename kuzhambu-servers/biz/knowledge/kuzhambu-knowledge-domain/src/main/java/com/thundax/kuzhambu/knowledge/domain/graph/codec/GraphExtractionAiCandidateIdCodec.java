package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionAiCandidateId;

public final class GraphExtractionAiCandidateIdCodec {

    private GraphExtractionAiCandidateIdCodec() {}

    public static GraphExtractionAiCandidateId toDomain(Long value) {
        return value == null ? null : new GraphExtractionAiCandidateId(value);
    }

    public static Long toValue(GraphExtractionAiCandidateId id) {
        return id == null ? null : id.value();
    }
}
