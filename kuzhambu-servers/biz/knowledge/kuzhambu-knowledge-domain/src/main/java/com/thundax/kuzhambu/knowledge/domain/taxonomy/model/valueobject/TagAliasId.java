package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class TagAliasId extends BaseLongId {

    private TagAliasId(Long value) {
        super(value);
    }

    public static TagAliasId of(Long value) {
        return new TagAliasId(value);
    }

    public static TagAliasId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
