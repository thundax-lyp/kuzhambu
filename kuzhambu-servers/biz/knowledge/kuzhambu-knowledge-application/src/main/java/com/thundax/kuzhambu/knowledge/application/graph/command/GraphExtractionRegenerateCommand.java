package com.thundax.kuzhambu.knowledge.application.graph.command;

public record GraphExtractionRegenerateCommand(
        Long taskId,
        long taskLockVersion,
        String expectedExecutionStatus,
        String expectedDisposition,
        String idempotencyKey,
        Long requestedBy) {}
