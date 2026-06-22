package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class TagCategoryId extends BaseLongId {

    private TagCategoryId(Long value) {
        super(value);
    }

    public static TagCategoryId of(Long value) {
        return new TagCategoryId(value);
    }

    public static TagCategoryId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
