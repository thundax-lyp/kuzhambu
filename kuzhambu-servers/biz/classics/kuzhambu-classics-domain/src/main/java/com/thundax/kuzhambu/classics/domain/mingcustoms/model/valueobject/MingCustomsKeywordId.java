package com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class MingCustomsKeywordId extends BaseLongId {

    private MingCustomsKeywordId(Long value) {
        super(value);
    }

    public static MingCustomsKeywordId of(Long value) {
        return new MingCustomsKeywordId(value);
    }

    public static MingCustomsKeywordId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
