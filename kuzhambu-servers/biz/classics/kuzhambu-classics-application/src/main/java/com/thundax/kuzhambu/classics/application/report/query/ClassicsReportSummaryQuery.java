package com.thundax.kuzhambu.classics.application.report.query;

import java.time.Instant;

public record ClassicsReportSummaryQuery(Instant periodStart, Instant periodEnd, String bucketType) {}
