package com.thundax.kuzhambu.classics.domain.sancai.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class SancaiVolumeId extends BaseLongId {

    private SancaiVolumeId(Long value) {
        super(value);
    }

    public static SancaiVolumeId of(Long value) {
        return new SancaiVolumeId(value);
    }

    public static SancaiVolumeId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
