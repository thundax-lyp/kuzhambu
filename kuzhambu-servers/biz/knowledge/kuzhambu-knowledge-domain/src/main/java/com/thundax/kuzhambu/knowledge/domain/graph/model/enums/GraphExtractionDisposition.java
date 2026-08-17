package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;

public enum GraphExtractionDisposition {
    PENDING,
    ADOPTED_MERGE,
    ADOPTED_REPLACE,
    DISCARDED,
    SUPERSEDED;

    public String value() {
        return name();
    }

    public static GraphExtractionDisposition from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Unsupported graph extraction disposition: " + value);
        }
    }
}
