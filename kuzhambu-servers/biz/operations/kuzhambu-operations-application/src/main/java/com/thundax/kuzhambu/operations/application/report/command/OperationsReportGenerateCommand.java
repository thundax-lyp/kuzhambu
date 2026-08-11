package com.thundax.kuzhambu.operations.application.report.command;

import java.time.Instant;

public record OperationsReportGenerateCommand(
        String reportType, String format, Instant periodStart, Instant periodEnd, Long requesterUserId) {}
