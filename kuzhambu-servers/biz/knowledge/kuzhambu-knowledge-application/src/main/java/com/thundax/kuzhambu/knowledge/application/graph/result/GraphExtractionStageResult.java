package com.thundax.kuzhambu.knowledge.application.graph.result;

import java.time.Instant;

public record GraphExtractionStageResult(
        int stageOrder,
        String stageName,
        String status,
        int progress,
        String inputSummaryJson,
        String outputSummaryJson,
        String failureReason,
        Instant startedAt,
        Instant completedAt) {}
