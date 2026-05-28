package com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class WangqiDocumentId extends BaseLongId {

    private WangqiDocumentId(Long value) {
        super(value);
    }

    public static WangqiDocumentId of(Long value) {
        return new WangqiDocumentId(value);
    }

    public static WangqiDocumentId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
