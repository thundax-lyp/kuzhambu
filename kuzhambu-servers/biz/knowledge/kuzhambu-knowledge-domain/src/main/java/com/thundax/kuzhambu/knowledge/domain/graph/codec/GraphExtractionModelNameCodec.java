package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionModelName;
import org.apache.commons.lang3.StringUtils;

public final class GraphExtractionModelNameCodec {

    private GraphExtractionModelNameCodec() {}

    public static GraphExtractionModelName toDomain(String value) {
        String normalized = StringUtils.trimToNull(value);
        return normalized == null ? null : new GraphExtractionModelName(normalized);
    }

    public static String toValue(GraphExtractionModelName modelName) {
        return modelName == null ? null : modelName.value();
    }
}
