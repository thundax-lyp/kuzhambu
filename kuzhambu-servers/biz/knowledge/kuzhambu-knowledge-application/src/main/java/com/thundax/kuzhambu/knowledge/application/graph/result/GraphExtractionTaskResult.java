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
        String failureReason,
        Instant requestedAt,
        Instant completedAt,
        Instant disposedAt,
        Instant purgeAfter,
        String materialTitle,
        String categoryName,
        String volumeName) {

    public GraphExtractionTaskResult(
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
            String failureReason,
            Instant requestedAt,
            Instant completedAt,
            Instant disposedAt,
            Instant purgeAfter,
            String materialTitle) {
        this(
                taskId,
                contentRef,
                executionStatus,
                disposition,
                attemptNo,
                lockVersion,
                batchId,
                candidateId,
                currentStage,
                progress,
                failureReason,
                requestedAt,
                completedAt,
                disposedAt,
                purgeAfter,
                materialTitle,
                null,
                null);
    }

    public GraphExtractionTaskResult(
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
            Instant purgeAfter) {
        this(
                taskId,
                contentRef,
                executionStatus,
                disposition,
                attemptNo,
                lockVersion,
                batchId,
                candidateId,
                currentStage,
                progress,
                null,
                requestedAt,
                completedAt,
                disposedAt,
                purgeAfter,
                null,
                null,
                null);
    }
}
