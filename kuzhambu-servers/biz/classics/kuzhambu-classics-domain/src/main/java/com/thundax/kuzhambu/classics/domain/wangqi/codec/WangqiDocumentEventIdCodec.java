package com.thundax.kuzhambu.classics.domain.wangqi.codec;

import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentEventId;

public final class WangqiDocumentEventIdCodec {

    private WangqiDocumentEventIdCodec() {}

    public static WangqiDocumentEventId toDomain(Long value) {
        return WangqiDocumentEventId.ofNullable(value);
    }

    public static Long toValue(WangqiDocumentEventId id) {
        return id == null ? null : id.value();
    }
}
