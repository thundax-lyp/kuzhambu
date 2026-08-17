package com.thundax.kuzhambu.knowledge.application.graph.result;

import java.util.List;

public record GraphExtractionBatchResult(String idempotencyKey, List<GraphExtractionTaskResult> tasks) {}
