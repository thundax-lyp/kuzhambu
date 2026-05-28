package com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class MingCustomsEntryId extends BaseLongId {

    private MingCustomsEntryId(Long value) {
        super(value);
    }

    public static MingCustomsEntryId of(Long value) {
        return new MingCustomsEntryId(value);
    }

    public static MingCustomsEntryId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
