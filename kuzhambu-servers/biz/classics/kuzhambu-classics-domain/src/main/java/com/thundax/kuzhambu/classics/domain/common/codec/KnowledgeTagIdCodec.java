package com.thundax.kuzhambu.classics.domain.common.codec;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.KnowledgeTagId;

public final class KnowledgeTagIdCodec {

    private KnowledgeTagIdCodec() {}

    public static KnowledgeTagId toDomain(Long value) {
        return KnowledgeTagId.ofNullable(value);
    }

    public static Long toValue(KnowledgeTagId id) {
        return id == null ? null : id.value();
    }
}
