package com.thundax.kuzhambu.classics.domain.sancai.codec;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;

public final class SancaiEntryImageIdCodec {

    private SancaiEntryImageIdCodec() {}

    public static SancaiEntryImageId toDomain(Long value) {
        return SancaiEntryImageId.ofNullable(value);
    }

    public static Long toValue(SancaiEntryImageId id) {
        return id == null ? null : id.value();
    }
}
