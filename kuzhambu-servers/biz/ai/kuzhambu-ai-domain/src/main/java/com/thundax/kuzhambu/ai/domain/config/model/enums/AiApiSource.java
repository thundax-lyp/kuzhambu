package com.thundax.kuzhambu.ai.domain.config.model.enums;

import java.util.Locale;

public enum AiApiSource {
    OPENAI_COMPATIBLE,
    OPENAI,
    BYTEDANCE;

    public String value() {
        return name();
    }

    public static AiApiSource from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return AiApiSource.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
