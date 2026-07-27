package com.thundax.kuzhambu.ai.domain.invocation.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum AiCandidateStatus {
    PENDING,
    APPLIED,
    REJECTED;

    public static AiCandidateStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AI-INVOCATION-400", "ai.candidate.status.invalid", "Unknown AI candidate status: " + value));
    }
}
