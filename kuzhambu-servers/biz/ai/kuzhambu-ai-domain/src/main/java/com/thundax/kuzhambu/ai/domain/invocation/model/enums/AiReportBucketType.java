package com.thundax.kuzhambu.ai.domain.invocation.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum AiReportBucketType {
    HOUR,
    DAY,
    WEEK,
    MONTH;

    public static AiReportBucketType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AI-10002", "ai.report-bucket-type.invalid", "Unknown AI report bucket type: " + value));
    }
}
