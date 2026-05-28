package com.thundax.kuzhambu.classics.domain.sancai.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class SancaiEntryImageId extends BaseLongId {

    private SancaiEntryImageId(Long value) {
        super(value);
    }

    public static SancaiEntryImageId of(Long value) {
        return new SancaiEntryImageId(value);
    }

    public static SancaiEntryImageId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
