package com.thundax.kuzhambu.classics.domain.mingcustoms.codec;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;

public final class MingCustomsKeywordIdCodec {

    private MingCustomsKeywordIdCodec() {}

    public static MingCustomsKeywordId toDomain(Long value) {
        return MingCustomsKeywordId.ofNullable(value);
    }

    public static Long toValue(MingCustomsKeywordId id) {
        return id == null ? null : id.value();
    }
}
