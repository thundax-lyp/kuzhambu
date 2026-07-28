package com.thundax.kuzhambu.classics.domain.sancai.codec;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;

public final class SancaiShowcaseIdCodec {

    private SancaiShowcaseIdCodec() {}

    public static SancaiShowcaseId toDomain(Long value) {
        return value == null ? null : new SancaiShowcaseId(value);
    }

    public static Long toValue(SancaiShowcaseId id) {
        return id == null ? null : id.value();
    }
}
