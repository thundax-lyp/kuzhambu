package com.thundax.kuzhambu.operations.application.health.query;

import java.time.Instant;

public record OperationsHealthQuery(
        String component,
        String healthStatus,
        String probeSource,
        String probeTarget,
        Instant checkedAtStart,
        Instant checkedAtEnd) {}
