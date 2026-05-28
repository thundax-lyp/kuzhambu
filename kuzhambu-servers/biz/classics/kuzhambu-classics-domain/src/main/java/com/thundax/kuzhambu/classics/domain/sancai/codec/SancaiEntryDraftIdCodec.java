package com.thundax.kuzhambu.classics.domain.sancai.codec;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryDraftId;

public final class SancaiEntryDraftIdCodec {

    private SancaiEntryDraftIdCodec() {}

    public static SancaiEntryDraftId toDomain(Long value) {
        return SancaiEntryDraftId.ofNullable(value);
    }

    public static Long toValue(SancaiEntryDraftId id) {
        return id == null ? null : id.value();
    }
}
