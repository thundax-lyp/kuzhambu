package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.helper.GraphKeyHelper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import java.util.Map;

public final class GraphEdgeKeyCodec {

    private GraphEdgeKeyCodec() {}

    public static GraphEdgeKey toDomain(
            GraphNodeKey sourceNodeKey,
            GraphNodeKey targetNodeKey,
            String relationType,
            boolean directed,
            Map<String, String> keyQualifiers) {
        return GraphKeyHelper.generateEdgeKey(sourceNodeKey, targetNodeKey, relationType, directed, keyQualifiers);
    }

    public static GraphEdgeKey toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new GraphEdgeKey(value);
    }

    public static String toValue(GraphEdgeKey key) {
        return key == null ? null : key.value();
    }
}
