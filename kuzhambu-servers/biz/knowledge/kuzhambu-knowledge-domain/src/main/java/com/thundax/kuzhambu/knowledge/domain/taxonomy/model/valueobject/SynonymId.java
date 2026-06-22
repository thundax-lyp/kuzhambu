package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class SynonymId extends BaseLongId {

    private SynonymId(Long value) {
        super(value);
    }

    public static SynonymId of(Long value) {
        return new SynonymId(value);
    }

    public static SynonymId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
