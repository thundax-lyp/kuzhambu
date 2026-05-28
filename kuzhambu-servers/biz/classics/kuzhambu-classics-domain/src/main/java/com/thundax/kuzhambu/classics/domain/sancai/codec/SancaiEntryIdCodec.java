package com.thundax.kuzhambu.classics.domain.sancai.codec;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;

public final class SancaiEntryIdCodec {

    private SancaiEntryIdCodec() {}

    public static SancaiEntryId toDomain(Long value) {
        return SancaiEntryId.ofNullable(value);
    }

    public static Long toValue(SancaiEntryId id) {
        return id == null ? null : id.value();
    }
}
