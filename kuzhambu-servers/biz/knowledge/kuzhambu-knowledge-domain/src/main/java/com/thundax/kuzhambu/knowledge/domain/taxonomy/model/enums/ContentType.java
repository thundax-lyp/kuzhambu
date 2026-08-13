package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ContentType {
    SANCAI_ENTRY,
    WANGQI_DOCUMENT,
    MING_CUSTOMS;

    public String value() {
        return name();
    }

    public static ContentType from(String value) {
        if ("MING_CUSTOM".equalsIgnoreCase(value)) {
            return MING_CUSTOMS;
        }
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "KNOWLEDGE-10005",
                        "knowledge.taxonomy.tag-content-ref.content-type.invalid",
                        "Unknown content type: " + value));
    }
}
