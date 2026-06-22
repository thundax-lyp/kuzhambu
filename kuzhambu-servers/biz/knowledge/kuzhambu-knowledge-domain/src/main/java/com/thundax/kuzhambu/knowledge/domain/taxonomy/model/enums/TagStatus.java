package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum TagStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static TagStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "KNOWLEDGE-10002", "knowledge.taxonomy.tag.status.invalid", "Unknown tag status: " + value));
    }
}
