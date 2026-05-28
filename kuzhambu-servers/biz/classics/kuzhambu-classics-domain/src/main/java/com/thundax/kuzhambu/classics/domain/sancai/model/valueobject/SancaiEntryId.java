package com.thundax.kuzhambu.classics.domain.sancai.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class SancaiEntryId extends BaseLongId {

    private SancaiEntryId(Long value) {
        super(value);
    }

    public static SancaiEntryId of(Long value) {
        return new SancaiEntryId(value);
    }

    public static SancaiEntryId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
