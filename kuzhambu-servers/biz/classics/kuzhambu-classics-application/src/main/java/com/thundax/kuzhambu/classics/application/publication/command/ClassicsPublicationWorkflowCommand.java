package com.thundax.kuzhambu.classics.application.publication.command;

import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import java.time.Instant;

public record ClassicsPublicationWorkflowCommand(
        ClassicsPublicationJob job,
        ClassicsPublicationJobId jobId,
        ClassicsPublicationExecutionToken executionToken,
        String cleanupToken,
        Instant occurredAt,
        Instant expiresAt,
        String failureReason,
        String detailJson,
        boolean es) {

    public static ClassicsPublicationWorkflowCommand cleanupClaim(
            ClassicsPublicationJob job, String cleanupToken, Instant now, Instant expiresAt, boolean es) {
        return new ClassicsPublicationWorkflowCommand(job, null, null, cleanupToken, now, expiresAt, null, null, es);
    }

    public static ClassicsPublicationWorkflowCommand cleanup(
            ClassicsPublicationJob job, String cleanupToken, boolean es) {
        return new ClassicsPublicationWorkflowCommand(job, null, null, cleanupToken, null, null, null, null, es);
    }

    public static ClassicsPublicationWorkflowCommand cleanupFailure(
            ClassicsPublicationJob job, String cleanupToken, boolean es, String detailJson) {
        return new ClassicsPublicationWorkflowCommand(job, null, null, cleanupToken, null, null, null, detailJson, es);
    }

    public static ClassicsPublicationWorkflowCommand executionClaim(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken executionToken,
            Instant now,
            Instant expiresAt) {
        return new ClassicsPublicationWorkflowCommand(
                null, jobId, executionToken, null, now, expiresAt, null, null, false);
    }

    public static ClassicsPublicationWorkflowCommand execution(
            ClassicsPublicationJobId jobId, ClassicsPublicationExecutionToken executionToken) {
        return new ClassicsPublicationWorkflowCommand(null, jobId, executionToken, null, null, null, null, null, false);
    }

    public static ClassicsPublicationWorkflowCommand executionStart(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken executionToken,
            Instant startedAt,
            Instant expiresAt) {
        return new ClassicsPublicationWorkflowCommand(
                null, jobId, executionToken, null, startedAt, expiresAt, null, null, false);
    }

    public static ClassicsPublicationWorkflowCommand executionFailure(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken executionToken,
            Instant occurredAt,
            String failureReason,
            String detailJson) {
        return new ClassicsPublicationWorkflowCommand(
                null, jobId, executionToken, null, occurredAt, null, failureReason, detailJson, false);
    }

    public static ClassicsPublicationWorkflowCommand executionRetry(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken executionToken,
            Instant nextRetryAt,
            String failureReason,
            String detailJson) {
        return executionFailure(jobId, executionToken, nextRetryAt, failureReason, detailJson);
    }

    public static ClassicsPublicationWorkflowCommand reconcileSuccess(ClassicsPublicationJob job, Instant finishedAt) {
        return new ClassicsPublicationWorkflowCommand(job, null, null, null, finishedAt, null, null, null, false);
    }

    public static ClassicsPublicationWorkflowCommand reconcileFailure(ClassicsPublicationJob job) {
        return new ClassicsPublicationWorkflowCommand(job, null, null, null, null, null, null, null, false);
    }

    public static ClassicsPublicationWorkflowCommand step(
            ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken) {
        return new ClassicsPublicationWorkflowCommand(job, null, executionToken, null, null, null, null, null, false);
    }
}
