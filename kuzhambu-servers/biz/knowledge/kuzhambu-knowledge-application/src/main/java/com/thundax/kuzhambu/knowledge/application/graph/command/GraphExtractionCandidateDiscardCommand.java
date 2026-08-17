package com.thundax.kuzhambu.knowledge.application.graph.command;

public record GraphExtractionCandidateDiscardCommand(Long taskId, long taskLockVersion) {}
