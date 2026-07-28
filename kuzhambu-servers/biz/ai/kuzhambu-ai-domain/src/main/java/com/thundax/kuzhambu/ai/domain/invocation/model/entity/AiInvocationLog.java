package com.thundax.kuzhambu.ai.domain.invocation.model.entity;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiInvocationLogId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiInvocationLog {

    private AiInvocationLogId id;
    private AiCallId callId;
    private AiBatchJobId batchId;
    private String scope;
    private AiBusinessCapability capability;
    private AiContentRef contentRef;
    private AiTargetObjectId targetObjectId;
    private Long serviceId;
    private String serviceRole;
    private AiModelId modelId;
    private AiModelName modelName;
    private PromptVersionId promptVersionId;
    private RequestId requestId;
    private TraceId traceId;
    private AiInvocationStatus status = AiInvocationStatus.RUNNING;
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
        this.status = AiInvocationStatus.SUCCEEDED;
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
        this.status = AiInvocationStatus.FAILED;
        this.usage = AiUsageSnapshot.orEmpty(usageSnapshot);
        this.errorType = failureType;
        this.errorMessage = failureMessage;
        this.completedAt = completedTime;
    }

    public void recordFailureStage(String stage) {
        this.failureStage = stage;
    }
}
