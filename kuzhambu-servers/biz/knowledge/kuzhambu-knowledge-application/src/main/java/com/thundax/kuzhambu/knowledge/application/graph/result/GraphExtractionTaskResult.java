package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.time.Instant;

public record GraphExtractionTaskResult(
        Long taskId,
        ContentRef contentRef,
        String executionStatus,
        String disposition,
        int attemptNo,
        long lockVersion,
        String batchId,
        Long candidateId,
        String currentStage,
        int progress,
        Instant requestedAt,
        Instant completedAt,
        Instant disposedAt,
        Instant purgeAfter) {}
