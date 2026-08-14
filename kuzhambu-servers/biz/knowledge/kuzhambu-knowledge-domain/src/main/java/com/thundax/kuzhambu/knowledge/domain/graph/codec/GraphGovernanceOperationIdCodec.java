package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphGovernanceOperationId;

public final class GraphGovernanceOperationIdCodec {

    private GraphGovernanceOperationIdCodec() {}

    public static GraphGovernanceOperationId toDomain(Long value) {
        return value == null ? null : new GraphGovernanceOperationId(value);
    }

    public static Long toValue(GraphGovernanceOperationId value) {
        return value == null ? null : value.value();
    }
}
