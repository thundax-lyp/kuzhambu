package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.time.Instant;

public record GraphExtractionResult(
        ContentRef materialRef,
        Long batchJobId,
        String status,
        int totalCount,
        int successCount,
        int failedCount,
        String inputSnapshot,
        String resultSummary,
        String failureReason,
        Instant requestedAt,
        Instant completedAt) {}
