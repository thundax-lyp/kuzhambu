package com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class GraphExtractionTaskId extends BaseLongId {

    private GraphExtractionTaskId(Long value) {
        super(value);
    }

    public static GraphExtractionTaskId of(Long value) {
        return new GraphExtractionTaskId(value);
    }

    public static GraphExtractionTaskId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
