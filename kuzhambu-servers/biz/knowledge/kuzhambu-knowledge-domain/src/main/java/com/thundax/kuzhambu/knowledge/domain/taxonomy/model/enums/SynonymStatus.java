package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SynonymStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static SynonymStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "KNOWLEDGE-10006",
                        "knowledge.taxonomy.synonym.status.invalid",
                        "Unknown synonym status: " + value));
    }
}
