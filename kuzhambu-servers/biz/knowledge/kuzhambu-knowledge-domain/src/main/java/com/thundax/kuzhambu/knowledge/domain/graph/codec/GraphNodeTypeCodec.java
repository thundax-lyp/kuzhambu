package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;

public final class GraphNodeTypeCodec {

    private GraphNodeTypeCodec() {}

    public static GraphNodeType toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return GraphNodeType.valueOf(value.trim());
    }

    public static String toValue(GraphNodeType nodeType) {
        return nodeType == null ? null : nodeType.name();
    }
}
