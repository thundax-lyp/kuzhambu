package com.thundax.kuzhambu.classics.domain.sancai.codec;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;

public final class SancaiVolumeIdCodec {

    private SancaiVolumeIdCodec() {}

    public static SancaiVolumeId toDomain(Long value) {
        return SancaiVolumeId.ofNullable(value);
    }

    public static Long toValue(SancaiVolumeId id) {
        return id == null ? null : id.value();
    }
}
