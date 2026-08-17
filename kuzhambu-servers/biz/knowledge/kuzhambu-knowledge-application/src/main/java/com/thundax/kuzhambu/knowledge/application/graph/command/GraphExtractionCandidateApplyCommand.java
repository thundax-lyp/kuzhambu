package com.thundax.kuzhambu.knowledge.application.graph.command;

public record GraphExtractionCandidateApplyCommand(
        Long taskId, long taskLockVersion, GraphMaterialApplyMode applyMode) {}
