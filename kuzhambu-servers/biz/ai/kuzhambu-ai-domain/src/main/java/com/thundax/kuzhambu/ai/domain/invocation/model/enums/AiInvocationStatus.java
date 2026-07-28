package com.thundax.kuzhambu.ai.domain.invocation.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum AiInvocationStatus {
    RUNNING,
    SUCCEEDED,
    FAILED,
    PARTIAL,
    CANCELLED;

    public static AiInvocationStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AI-INVOCATION-400", "ai.invocation.status.invalid", "Unknown AI invocation status: " + value));
    }
}
