package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum TagCategoryStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static TagCategoryStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "KNOWLEDGE-10001",
                        "knowledge.taxonomy.tag-category.status.invalid",
                        "Unknown tag category status: " + value));
    }
}
