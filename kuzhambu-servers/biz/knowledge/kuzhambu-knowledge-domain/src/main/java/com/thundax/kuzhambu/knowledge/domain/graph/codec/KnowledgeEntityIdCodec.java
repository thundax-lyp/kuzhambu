package com.thundax.kuzhambu.knowledge.domain.graph.codec;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.KnowledgeEntityId;

public final class KnowledgeEntityIdCodec {

    private KnowledgeEntityIdCodec() {}

    public static KnowledgeEntityId toDomain(Long value) {
        return value == null ? null : new KnowledgeEntityId(value);
    }

    public static Long toValue(KnowledgeEntityId id) {
        return id == null ? null : id.value();
    }
}
