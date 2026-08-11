package com.thundax.kuzhambu.operations.application.report.query;

import java.time.Instant;

public record OperationsReportQuery(
        String reportType,
        String format,
        String reportStatus,
        Long requesterUserId,
        Instant periodStart,
        Instant periodEnd) {}
