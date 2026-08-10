package com.thundax.kuzhambu.classics.application.publication.assembler;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationWorkflowCommand;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import java.time.Instant;
import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationFacadeAssembler {

    @NonNull
    public ClassicsPublicationWorkflowCommand toExecutionClaimCommand(
            @NonNull ClassicsPublicationJobId jobId,
            @NonNull ClassicsPublicationExecutionToken executionToken,
            @NonNull Instant occurredAt,
            @NonNull Instant expiresAt) {
        Objects.requireNonNull(jobId, "jobId");
        Objects.requireNonNull(executionToken, "executionToken");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        return new ClassicsPublicationWorkflowCommand(
                null, jobId, executionToken, null, occurredAt, expiresAt, null, null, false);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toExecutionStartCommand(
            @NonNull ClassicsPublicationJobId jobId,
            @NonNull ClassicsPublicationExecutionToken executionToken,
            @NonNull Instant occurredAt,
            @NonNull Instant expiresAt) {
        Objects.requireNonNull(jobId, "jobId");
        Objects.requireNonNull(executionToken, "executionToken");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        return new ClassicsPublicationWorkflowCommand(
                null, jobId, executionToken, null, occurredAt, expiresAt, null, null, false);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toExecutionReleaseCommand(
            @NonNull ClassicsPublicationJobId jobId, @NonNull ClassicsPublicationExecutionToken executionToken) {
        Objects.requireNonNull(jobId, "jobId");
        Objects.requireNonNull(executionToken, "executionToken");
        return new ClassicsPublicationWorkflowCommand(null, jobId, executionToken, null, null, null, null, null, false);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toExecutionRetryCommand(
            @NonNull ClassicsPublicationJobId jobId,
            @NonNull ClassicsPublicationExecutionToken executionToken,
            @NonNull Instant occurredAt,
            @NonNull String failureReason,
            @NonNull String detailJson) {
        Objects.requireNonNull(jobId, "jobId");
        Objects.requireNonNull(executionToken, "executionToken");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(failureReason, "failureReason");
        Objects.requireNonNull(detailJson, "detailJson");
        return new ClassicsPublicationWorkflowCommand(
                null, jobId, executionToken, null, occurredAt, null, failureReason, detailJson, false);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toExecutionFailureCommand(
            @NonNull ClassicsPublicationJobId jobId,
            @NonNull ClassicsPublicationExecutionToken executionToken,
            @NonNull Instant occurredAt,
            @NonNull String failureReason,
            @NonNull String detailJson) {
        Objects.requireNonNull(jobId, "jobId");
        Objects.requireNonNull(executionToken, "executionToken");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(failureReason, "failureReason");
        Objects.requireNonNull(detailJson, "detailJson");
        return new ClassicsPublicationWorkflowCommand(
                null, jobId, executionToken, null, occurredAt, null, failureReason, detailJson, false);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toCleanupClaimCommand(
            @NonNull ClassicsPublicationJob job,
            @NonNull String cleanupToken,
            boolean es,
            @NonNull Instant occurredAt,
            @NonNull Instant expiresAt) {
        Objects.requireNonNull(job, "job");
        Objects.requireNonNull(cleanupToken, "cleanupToken");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        return new ClassicsPublicationWorkflowCommand(
                job, null, null, cleanupToken, occurredAt, expiresAt, null, null, es);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toCleanupQualifyCommand(
            @NonNull ClassicsPublicationJob job, @NonNull String cleanupToken, boolean es) {
        Objects.requireNonNull(job, "job");
        Objects.requireNonNull(cleanupToken, "cleanupToken");
        return new ClassicsPublicationWorkflowCommand(job, null, null, cleanupToken, null, null, null, null, es);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toCleanupCompleteCommand(
            @NonNull ClassicsPublicationJob job, @NonNull String cleanupToken, boolean es) {
        Objects.requireNonNull(job, "job");
        Objects.requireNonNull(cleanupToken, "cleanupToken");
        return new ClassicsPublicationWorkflowCommand(job, null, null, cleanupToken, null, null, null, null, es);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toCleanupFailCommand(
            @NonNull ClassicsPublicationJob job, @NonNull String cleanupToken, boolean es, @NonNull String detailJson) {
        Objects.requireNonNull(job, "job");
        Objects.requireNonNull(cleanupToken, "cleanupToken");
        Objects.requireNonNull(detailJson, "detailJson");
        return new ClassicsPublicationWorkflowCommand(job, null, null, cleanupToken, null, null, null, detailJson, es);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toReconcileSucceedCommand(
            @NonNull ClassicsPublicationJob job, @NonNull Instant occurredAt) {
        Objects.requireNonNull(job, "job");
        Objects.requireNonNull(occurredAt, "occurredAt");
        return new ClassicsPublicationWorkflowCommand(job, null, null, null, occurredAt, null, null, null, false);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toReconcileFailureCommand(@NonNull ClassicsPublicationJob job) {
        Objects.requireNonNull(job, "job");
        return new ClassicsPublicationWorkflowCommand(job, null, null, null, null, null, null, null, false);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toSnapshotBindCommand(
            @NonNull ClassicsPublicationJob job, @NonNull ClassicsPublicationExecutionToken executionToken) {
        Objects.requireNonNull(job, "job");
        Objects.requireNonNull(executionToken, "executionToken");
        return new ClassicsPublicationWorkflowCommand(job, null, executionToken, null, null, null, null, null, false);
    }

    @NonNull
    public ClassicsPublicationWorkflowCommand toContentCommitCommand(
            @NonNull ClassicsPublicationJob job, @NonNull ClassicsPublicationExecutionToken executionToken) {
        Objects.requireNonNull(job, "job");
        Objects.requireNonNull(executionToken, "executionToken");
        return new ClassicsPublicationWorkflowCommand(job, null, executionToken, null, null, null, null, null, false);
    }
}
