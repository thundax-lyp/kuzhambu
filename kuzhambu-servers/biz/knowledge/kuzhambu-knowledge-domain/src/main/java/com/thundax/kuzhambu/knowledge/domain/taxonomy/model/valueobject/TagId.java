package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class TagId extends BaseLongId {

    private TagId(Long value) {
        super(value);
    }

    public static TagId of(Long value) {
        return new TagId(value);
    }

    public static TagId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
