package com.thundax.kuzhambu.knowledge.application.graph.command;

public record GraphExtractionCancelCommand(
        Long taskId, long taskLockVersion, String expectedExecutionStatus, String idempotencyKey) {}
