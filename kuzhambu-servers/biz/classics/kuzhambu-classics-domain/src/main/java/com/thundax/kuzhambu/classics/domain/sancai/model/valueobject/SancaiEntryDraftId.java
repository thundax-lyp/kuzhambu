package com.thundax.kuzhambu.classics.domain.sancai.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class SancaiEntryDraftId extends BaseLongId {

    private SancaiEntryDraftId(Long value) {
        super(value);
    }

    public static SancaiEntryDraftId of(Long value) {
        return new SancaiEntryDraftId(value);
    }

    public static SancaiEntryDraftId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
