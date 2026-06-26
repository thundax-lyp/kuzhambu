package com.thundax.kuzhambu.operations.domain.report.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ReportStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED;

    public String value() {
        return name();
    }

    public static ReportStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "OPERATIONS-REPORT-400",
                        "operations.report.status.invalid",
                        "Unknown operations report status: " + value));
    }
}
