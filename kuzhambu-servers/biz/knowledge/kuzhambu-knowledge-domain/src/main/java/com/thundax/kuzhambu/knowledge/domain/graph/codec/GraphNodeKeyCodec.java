package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.helper.GraphKeyHelper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;

public final class GraphNodeKeyCodec {

    private GraphNodeKeyCodec() {}

    public static GraphNodeKey toDomain(GraphNodeType nodeType, String name, String identityQualifier) {
        return GraphKeyHelper.generateNodeKey(nodeType, name, identityQualifier);
    }

    public static GraphNodeKey toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new GraphNodeKey(value);
    }

    public static String toValue(GraphNodeKey key) {
        return key == null ? null : key.value();
    }
}
