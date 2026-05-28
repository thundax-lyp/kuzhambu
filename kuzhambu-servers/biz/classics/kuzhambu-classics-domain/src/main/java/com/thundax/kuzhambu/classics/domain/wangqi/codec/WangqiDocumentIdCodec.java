package com.thundax.kuzhambu.classics.domain.wangqi.codec;

import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;

public final class WangqiDocumentIdCodec {

    private WangqiDocumentIdCodec() {}

    public static WangqiDocumentId toDomain(Long value) {
        return WangqiDocumentId.ofNullable(value);
    }

    public static Long toValue(WangqiDocumentId id) {
        return id == null ? null : id.value();
    }
}
