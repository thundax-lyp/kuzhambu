package com.thundax.kuzhambu.ai.domain.config.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum PromptTemplateStatus {
    ACTIVE,
    INACTIVE;

    public String value() {
        return name();
    }

    public static PromptTemplateStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AI-10003", "ai.prompt-template-status.invalid", "Unknown prompt template status: " + value));
    }

    public static PromptTemplateStatus fromNullable(String value) {
        return value == null || value.isBlank() ? ACTIVE : from(value);
    }
}
