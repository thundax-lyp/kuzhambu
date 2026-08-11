package com.thundax.kuzhambu.operations.application.cleanup.command;

import java.time.Instant;

public record OperationsCleanupExecuteCommand(
        String cleanupType, Long requesterUserId, Instant requestedAt, Integer retentionDays, Integer limit) {

    public OperationsCleanupExecuteCommand(String cleanupType, Long requesterUserId) {
        this(cleanupType, requesterUserId, null, null, null);
    }
}
