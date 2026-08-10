package com.thundax.kuzhambu.classics.application.cleanup.query;

import java.time.Instant;

public record ClassicsCleanupTargetsQuery(
        String cleanupType, Instant requestedAt, Integer retentionDays, Integer maxTargets) {}
