package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphExtractionTask {
    private GraphExtractionTaskId id;
    private Long materialId;
    private ContentRef contentRef;
    private String contentSnapshotJson;
    private String modelSnapshotJson;
    private String promptSnapshotJson;
    private String outputSchemaJson;
    private GraphExtractionExecutionStatus executionStatus;
    private GraphExtractionDisposition disposition;
    private int attemptNo;
    private long lockVersion;
    private String batchId;
    private Long aiBatchId;
    private Long candidateId;
    private String currentStage;
    private int progress;
    private String failureReason;
    private String idempotencyKey;
    private GraphExtractionTaskId regeneratedFromTaskId;
    private GraphExtractionTaskId supersededByTaskId;
    private GraphExtractionTaskId triggeredByTaskId;
    private Instant requestedAt;
    private Instant completedAt;
    private Instant disposedAt;
    private Instant purgeAfter;

    public void start() {
        requireExecutionStatus(GraphExtractionExecutionStatus.PENDING, "Only pending graph extraction tasks can start");
        executionStatus = GraphExtractionExecutionStatus.RUNNING;
    }

    public void resetToPendingForRecovery() {
        requireExecutionStatus(
                GraphExtractionExecutionStatus.RUNNING, "Only running graph extraction tasks can reset for recovery");
        executionStatus = GraphExtractionExecutionStatus.PENDING;
        currentStage = "PENDING";
    }

    public void succeed(Long resolvedCandidateId, String stage, int resolvedProgress, Instant completedAt) {
        requireExecutionStatus(
                GraphExtractionExecutionStatus.RUNNING, "Only running graph extraction tasks can succeed");
        candidateId = resolvedCandidateId;
        currentStage = stage;
        progress = resolvedProgress;
        executionStatus = GraphExtractionExecutionStatus.SUCCEEDED;
        disposition = GraphExtractionDisposition.PENDING;
        this.completedAt = completedAt;
    }

    public void fail(String stage, String failureReason, Instant completedAt) {
        requireExecutionStatus(GraphExtractionExecutionStatus.RUNNING, "Only running graph extraction tasks can fail");
        currentStage = stage;
        this.failureReason = failureReason;
        executionStatus = GraphExtractionExecutionStatus.FAILED;
        this.completedAt = completedAt;
    }

    public void retry() {
        requireExecutionStatus(GraphExtractionExecutionStatus.FAILED, "Only failed graph extraction tasks can retry");
        executionStatus = GraphExtractionExecutionStatus.PENDING;
        currentStage = null;
        failureReason = null;
        progress = 0;
        completedAt = null;
        attemptNo++;
    }

    public void markRegeneratedBy(GraphExtractionTaskId nextTaskId) {
        requireExecutionStatus(
                GraphExtractionExecutionStatus.FAILED, "Only failed graph extraction tasks can regenerate");
        if (nextTaskId == null) {
            throw new DomainException("Regenerated graph extraction task id is required");
        }
        supersededByTaskId = nextTaskId;
    }

    public boolean canDelete() {
        if (executionStatus == GraphExtractionExecutionStatus.FAILED
                || executionStatus == GraphExtractionExecutionStatus.CANCELLED) {
            return true;
        }
        return executionStatus == GraphExtractionExecutionStatus.SUCCEEDED
                && disposition != null
                && disposition != GraphExtractionDisposition.PENDING;
    }

    public void cancel(Instant completedAt) {
        if (executionStatus != GraphExtractionExecutionStatus.PENDING
                && executionStatus != GraphExtractionExecutionStatus.RUNNING) {
            throw new DomainException("Only pending or running graph extraction tasks can cancel");
        }
        executionStatus = GraphExtractionExecutionStatus.CANCELLED;
        this.completedAt = completedAt;
    }

    public void adopt(GraphExtractionDisposition adoption, Instant disposedAt, Instant purgeAfter) {
        requireSuccessfulPendingDisposition();
        if (adoption != GraphExtractionDisposition.ADOPTED_MERGE
                && adoption != GraphExtractionDisposition.ADOPTED_REPLACE) {
            throw new DomainException("Graph extraction adoption disposition is invalid");
        }
        dispose(adoption, disposedAt, purgeAfter);
    }

    public void discard(Instant disposedAt, Instant purgeAfter) {
        requireSuccessfulPendingDisposition();
        dispose(GraphExtractionDisposition.DISCARDED, disposedAt, purgeAfter);
    }

    public void supersede(GraphExtractionTaskId nextTaskId, Instant disposedAt, Instant purgeAfter) {
        requireSuccessfulPendingDisposition();
        if (nextTaskId == null) {
            throw new DomainException("Superseding graph extraction task id is required");
        }
        supersededByTaskId = nextTaskId;
        dispose(GraphExtractionDisposition.SUPERSEDED, disposedAt, purgeAfter);
    }

    private void requireSuccessfulPendingDisposition() {
        requireExecutionStatus(
                GraphExtractionExecutionStatus.SUCCEEDED, "Only succeeded graph extraction tasks can dispose");
        if (disposition != GraphExtractionDisposition.PENDING) {
            throw new DomainException("Graph extraction task disposition is no longer pending");
        }
    }

    private void dispose(GraphExtractionDisposition target, Instant disposedAt, Instant purgeAfter) {
        disposition = target;
        this.disposedAt = disposedAt;
        this.purgeAfter = purgeAfter;
    }

    private void requireExecutionStatus(GraphExtractionExecutionStatus expected, String message) {
        if (executionStatus != expected) {
            throw new DomainException(message);
        }
    }
}
