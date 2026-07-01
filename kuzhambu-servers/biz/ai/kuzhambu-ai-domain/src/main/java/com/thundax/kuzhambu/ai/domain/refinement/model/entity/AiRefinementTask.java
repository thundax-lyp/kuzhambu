package com.thundax.kuzhambu.ai.domain.refinement.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiRefinementTask {

    private Long id;
    private Long taskId;
    private String scope;
    private String capability;
    private String contentType;
    private Long contentId;
    private Long objectId;
    private Long requestedBy;
    private String requestId;
    private String traceId;
    private String status = "PENDING";
    private String serviceRole;
    private Long modelId;
    private String modelName;
    private Long promptVersionId;
    private Long callId;
    private Long candidateId;
    private String resultFormat;
    private String resultPreview;
    private String failureStage;
    private String errorType;
    private String errorMessage;
    private boolean streamEnabled;
    private Instant requestedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    public void markRunning(Instant startedTime) {
        this.status = "RUNNING";
        this.startedAt = startedTime;
        this.failureStage = null;
        this.errorType = null;
        this.errorMessage = null;
    }

    public void markSucceeded(
            Long callId, Long candidateId, String resultFormat, String resultPreview, Instant completedTime) {
        this.status = "SUCCEEDED";
        this.callId = callId;
        this.candidateId = candidateId;
        this.resultFormat = resultFormat;
        this.resultPreview = resultPreview;
        this.completedAt = completedTime;
        this.failureStage = null;
        this.errorType = null;
        this.errorMessage = null;
    }

    public void markFailed(String failureStage, String errorType, String errorMessage, Instant completedTime) {
        this.status = "FAILED";
        this.failureStage = failureStage;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.completedAt = completedTime;
    }

    public void markCancelled(Instant cancelledTime) {
        this.status = "CANCELLED";
        this.cancelledAt = cancelledTime;
    }
}
