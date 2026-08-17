package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;

public enum GraphExtractionExecutionStatus {
    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELLED;

    public String value() {
        return name();
    }

    public static GraphExtractionExecutionStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Unsupported graph extraction execution status: " + value);
        }
    }
}
