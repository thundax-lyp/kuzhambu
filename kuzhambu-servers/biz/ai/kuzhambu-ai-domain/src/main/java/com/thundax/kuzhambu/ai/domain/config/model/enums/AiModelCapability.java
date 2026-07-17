package com.thundax.kuzhambu.ai.domain.config.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum AiModelCapability {
    TEXT_TO_TEXT,
    TEXT_TO_IMAGE,
    IMAGE_TO_TEXT,
    IMAGE_TO_IMAGE;

    public String value() {
        return name();
    }

    public static AiModelCapability from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AI-10002", "ai.model-capability.invalid", "Unknown AI model capability: " + value));
    }
}
