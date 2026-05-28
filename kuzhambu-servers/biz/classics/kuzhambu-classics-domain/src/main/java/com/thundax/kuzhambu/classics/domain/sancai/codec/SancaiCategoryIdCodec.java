package com.thundax.kuzhambu.classics.domain.sancai.codec;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;

public final class SancaiCategoryIdCodec {

    private SancaiCategoryIdCodec() {}

    public static SancaiCategoryId toDomain(Long value) {
        return SancaiCategoryId.ofNullable(value);
    }

    public static Long toValue(SancaiCategoryId id) {
        return id == null ? null : id.value();
    }
}
