package com.thundax.kuzhambu.discovery.application.report.query;

import java.time.Instant;

public record DiscoveryReportSummaryQuery(Instant periodStart, Instant periodEnd, String bucketType) {}
