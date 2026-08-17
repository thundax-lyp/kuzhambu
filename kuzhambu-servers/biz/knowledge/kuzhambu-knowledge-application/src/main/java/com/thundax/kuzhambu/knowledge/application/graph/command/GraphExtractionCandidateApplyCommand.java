package com.thundax.kuzhambu.knowledge.application.graph.command;

public record GraphExtractionCandidateApplyCommand(
        Long taskId,
        long taskLockVersion,
        String expectedExecutionStatus,
        String expectedDisposition,
        long materialLockVersion,
        GraphMaterialApplyMode applyMode,
        String idempotencyKey) {}
