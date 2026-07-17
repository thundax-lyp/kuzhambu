package com.thundax.kuzhambu.ai.domain.config.codec;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;

public final class AiBusinessConfigIdCodec {

    private AiBusinessConfigIdCodec() {}

    public static AiBusinessConfigId toDomain(Long value) {
        return value == null ? null : new AiBusinessConfigId(value);
    }

    public static Long toValue(AiBusinessConfigId id) {
        return id == null ? null : id.value();
    }
}
