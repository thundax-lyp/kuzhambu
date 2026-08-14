package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionChangeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionTaskId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMaterialDeletionTask {
    private GraphMaterialDeletionTaskId id;
    private GraphMaterialDeletionChangeId deletionChangeId;
    private String idempotencyKey;
    private GraphMaterialDeletionStatus status;
    private long lockVersion;
    private int progress;
    private String failureReason;
    private String resultSummaryJson;
    private Instant requestedAt;
    private Instant completedAt;

    public void requireLockVersion(long expectedLockVersion) {
        if (lockVersion != expectedLockVersion) {
            throw GraphMaterialDeletionChange.lockConflict();
        }
    }

    public void startRunning() {
        if (status != GraphMaterialDeletionStatus.PENDING) {
            throw new DomainException("Only pending graph material deletion tasks can run");
        }
        status = GraphMaterialDeletionStatus.RUNNING;
        failureReason = null;
    }

    public void succeed(String resultSummaryJson, Instant completedAt) {
        if (status != GraphMaterialDeletionStatus.RUNNING) {
            throw new DomainException("Only running graph material deletion tasks can succeed");
        }
        status = GraphMaterialDeletionStatus.SUCCEEDED;
        progress = 100;
        failureReason = null;
        this.resultSummaryJson = resultSummaryJson;
        this.completedAt = completedAt;
    }

    public void fail(String failureReason, Instant completedAt) {
        if (status != GraphMaterialDeletionStatus.RUNNING) {
            throw new DomainException("Only running graph material deletion tasks can fail");
        }
        status = GraphMaterialDeletionStatus.FAILED;
        this.failureReason = failureReason;
        this.completedAt = completedAt;
    }

    public void retry() {
        if (status != GraphMaterialDeletionStatus.FAILED) {
            throw new DomainException("Only failed graph material deletion tasks can retry");
        }
        status = GraphMaterialDeletionStatus.PENDING;
        progress = 0;
        failureReason = null;
        completedAt = null;
    }
}
