package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum TagSource {
    MANUAL,
    AI_EXTRACTED;

    public String value() {
        return name();
    }

    public static TagSource from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "KNOWLEDGE-10003", "knowledge.taxonomy.tag.source.invalid", "Unknown tag source: " + value));
    }
}
