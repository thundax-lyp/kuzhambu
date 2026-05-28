package com.thundax.kuzhambu.classics.domain.sancai.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class SancaiShowcaseId extends BaseLongId {

    private SancaiShowcaseId(Long value) {
        super(value);
    }

    public static SancaiShowcaseId of(Long value) {
        return new SancaiShowcaseId(value);
    }

    public static SancaiShowcaseId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
