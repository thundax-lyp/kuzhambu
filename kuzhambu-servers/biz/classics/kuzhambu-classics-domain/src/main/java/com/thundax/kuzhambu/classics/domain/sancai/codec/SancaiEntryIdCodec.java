package com.thundax.kuzhambu.classics.domain.sancai.codec;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;

public final class SancaiEntryIdCodec {

    private SancaiEntryIdCodec() {}

    public static SancaiEntryId toDomain(Long value) {
        return value == null ? null : new SancaiEntryId(value);
    }

    public static Long toValue(SancaiEntryId id) {
        return id == null ? null : id.value();
    }
}
