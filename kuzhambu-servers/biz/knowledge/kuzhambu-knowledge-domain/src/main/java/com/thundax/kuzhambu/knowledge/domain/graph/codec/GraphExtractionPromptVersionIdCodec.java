package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionPromptVersionId;

public final class GraphExtractionPromptVersionIdCodec {

    private GraphExtractionPromptVersionIdCodec() {}

    public static GraphExtractionPromptVersionId toDomain(Long value) {
        return value == null ? null : new GraphExtractionPromptVersionId(value);
    }

    public static Long toValue(GraphExtractionPromptVersionId id) {
        return id == null ? null : id.value();
    }
}
