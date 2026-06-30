package com.thundax.kuzhambu.ai.domain.invocation.model.entity;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiCallRecord {

    private Long id;
    private Long callId;
    private Long batchId;
    private String scope;
    private String capability;
    private String contentType;
    private Long contentId;
    private Long objectId;
    private Long serviceId;
    private String serviceRole;
    private Long modelId;
    private String modelName;
    private Long promptVersionId;
    private String requestId;
    private String traceId;
    private String status = "RUNNING";
    private boolean streamUsed;
    private boolean streamCompleted;
    private boolean fallbackUsed;
    private AiUsageSnapshot usage = AiUsageSnapshot.empty();
    private String failureStage;
    private String resultFormat;
    private String resultPayload;
    private String artifactReferenceJson;
    private String errorType;
    private String errorMessage;
    private String warningsJson;
    private Instant requestedAt;
    private Instant completedAt;

    public void markSucceeded(AiUsageSnapshot usageSnapshot, Instant completedTime) {
        this.status = "SUCCEEDED";
        this.streamCompleted = streamUsed;
        this.usage = AiUsageSnapshot.orEmpty(usageSnapshot);
        this.completedAt = completedTime;
        this.errorType = null;
        this.errorMessage = null;
        this.failureStage = null;
    }

    public void recordResult(String format, String payload, String artifactReference, String warnings) {
        this.resultFormat = format;
        this.resultPayload = payload;
        this.artifactReferenceJson = artifactReference;
        this.warningsJson = warnings;
    }

    public void markFailed(
            String failureType, String failureMessage, AiUsageSnapshot usageSnapshot, Instant completedTime) {
        this.status = "FAILED";
        this.usage = AiUsageSnapshot.orEmpty(usageSnapshot);
        this.errorType = failureType;
        this.errorMessage = failureMessage;
        this.completedAt = completedTime;
    }

    public void recordFailureStage(String stage) {
        this.failureStage = stage;
    }
}
