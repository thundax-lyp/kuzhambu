package com.thundax.kuzhambu.classics.domain.common.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class KnowledgeTagId extends BaseLongId {

    private KnowledgeTagId(Long value) {
        super(value);
    }

    public static KnowledgeTagId of(Long value) {
        return new KnowledgeTagId(value);
    }

    public static KnowledgeTagId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
