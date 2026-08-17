package com.thundax.kuzhambu.knowledge.application.graph.command;

public record GraphExtractionRegenerateCommand(Long taskId, String idempotencyKey, Long requestedBy) {}
