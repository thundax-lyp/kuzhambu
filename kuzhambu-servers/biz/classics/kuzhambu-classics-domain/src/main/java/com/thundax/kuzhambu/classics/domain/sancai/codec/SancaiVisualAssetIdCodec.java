package com.thundax.kuzhambu.classics.domain.sancai.codec;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;

public final class SancaiVisualAssetIdCodec {

    private SancaiVisualAssetIdCodec() {}

    public static SancaiVisualAssetId toDomain(Long value) {
        return SancaiVisualAssetId.ofNullable(value);
    }

    public static Long toValue(SancaiVisualAssetId id) {
        return id == null ? null : id.value();
    }
}
