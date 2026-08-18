package com.thundax.kuzhambu.knowledge.application.graph.command;

public record GraphExtractionRetryCommand(
        Long taskId, long taskLockVersion, String expectedExecutionStatus, String idempotencyKey, Long requestedBy) {}
