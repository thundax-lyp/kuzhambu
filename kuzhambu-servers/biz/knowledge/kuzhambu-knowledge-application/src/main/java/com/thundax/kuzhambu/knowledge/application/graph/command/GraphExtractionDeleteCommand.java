package com.thundax.kuzhambu.knowledge.application.graph.command;

public record GraphExtractionDeleteCommand(
        Long taskId, long taskLockVersion, String expectedExecutionStatus, String idempotencyKey) {}
