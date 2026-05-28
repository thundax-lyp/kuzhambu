package com.thundax.kuzhambu.classics.domain.mingcustoms.codec;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;

public final class MingCustomsEntryIdCodec {

    private MingCustomsEntryIdCodec() {}

    public static MingCustomsEntryId toDomain(Long value) {
        return MingCustomsEntryId.ofNullable(value);
    }

    public static Long toValue(MingCustomsEntryId id) {
        return id == null ? null : id.value();
    }
}
