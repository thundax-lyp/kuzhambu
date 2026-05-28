package com.thundax.kuzhambu.classics.domain.sancai.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class SancaiVisualAssetId extends BaseLongId {

    private SancaiVisualAssetId(Long value) {
        super(value);
    }

    public static SancaiVisualAssetId of(Long value) {
        return new SancaiVisualAssetId(value);
    }

    public static SancaiVisualAssetId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
