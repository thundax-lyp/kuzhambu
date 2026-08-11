package com.thundax.kuzhambu.operations.application.health.query;

import java.time.Instant;

public record OperationsHealthTrendQuery(
        String component, String probeSource, Instant periodStart, Instant periodEnd, String bucketType) {}
