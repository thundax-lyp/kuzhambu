package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;

public final class GraphSourceTypeCodec {

    private GraphSourceTypeCodec() {}

    public static GraphSourceType toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return GraphSourceType.valueOf(value.trim());
    }

    public static String toValue(GraphSourceType sourceType) {
        return sourceType == null ? null : sourceType.name();
    }
}
