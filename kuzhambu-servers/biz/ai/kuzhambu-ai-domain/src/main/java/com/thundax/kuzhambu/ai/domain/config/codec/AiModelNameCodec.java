package com.thundax.kuzhambu.ai.domain.config.codec;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;

public final class AiModelNameCodec {

    private AiModelNameCodec() {}

    public static AiModelName toDomain(String value) {
        return AiModelName.ofNullable(value);
    }

    public static String toValue(AiModelName name) {
        return name == null ? null : name.value();
    }
}
