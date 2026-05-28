package com.thundax.kuzhambu.classics.domain.sancai.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class SancaiCategoryId extends BaseLongId {

    private SancaiCategoryId(Long value) {
        super(value);
    }

    public static SancaiCategoryId of(Long value) {
        return new SancaiCategoryId(value);
    }

    public static SancaiCategoryId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
