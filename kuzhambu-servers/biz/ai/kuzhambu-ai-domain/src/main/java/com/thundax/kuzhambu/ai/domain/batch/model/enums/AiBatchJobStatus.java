package com.thundax.kuzhambu.ai.domain.batch.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum AiBatchJobStatus {
    RUNNING,
    SUCCEEDED,
    PARTIAL,
    CANCELLED;

    public static AiBatchJobStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AI-BATCH-400", "ai.batch-job.status.invalid", "Unknown AI batch job status: " + value));
    }
}
