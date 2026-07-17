package com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class WangqiDocumentEventId extends BaseLongId {

    private WangqiDocumentEventId(Long value) {
        super(value);
    }

    public static WangqiDocumentEventId of(Long value) {
        return new WangqiDocumentEventId(value);
    }

    public static WangqiDocumentEventId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
