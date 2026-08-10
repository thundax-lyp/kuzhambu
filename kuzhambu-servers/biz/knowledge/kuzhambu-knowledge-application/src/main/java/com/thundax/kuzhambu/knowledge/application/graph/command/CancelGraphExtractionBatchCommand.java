package com.thundax.kuzhambu.knowledge.application.graph.command;

public record CancelGraphExtractionBatchCommand(Long batchJobId, Long requestedBy) {}
