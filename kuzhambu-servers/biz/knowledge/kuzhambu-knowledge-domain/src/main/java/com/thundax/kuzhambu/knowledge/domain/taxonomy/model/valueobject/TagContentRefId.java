package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class TagContentRefId extends BaseLongId {

    private TagContentRefId(Long value) {
        super(value);
    }

    public static TagContentRefId of(Long value) {
        return new TagContentRefId(value);
    }

    public static TagContentRefId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
