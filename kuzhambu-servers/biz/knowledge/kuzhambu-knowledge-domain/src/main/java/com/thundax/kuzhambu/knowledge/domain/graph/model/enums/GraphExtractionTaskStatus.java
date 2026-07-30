package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public enum GraphExtractionTaskStatus {
    REQUESTED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    APPLIED,
    CANCELLED,
    PARTIAL;

    public String value() {
        return name();
    }

    public static GraphExtractionTaskStatus from(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "KNOWLEDGE-30002",
                        "knowledge.graph.extraction.task.status.invalid",
                        "Unknown graph extraction task status: " + value));
    }
}
