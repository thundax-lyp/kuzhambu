package com.thundax.kuzhambu.ai.application.invocation.result;

import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
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
public class AiStreamEventResult {

    private String eventType;
    private String eventId;
    private RequestId requestId;
    private TraceId traceId;
    private String stage;
    private Instant timestamp;
    private String deltaText;
    private AiInvocationStatus status;
    private String resultFormat;
    private String resultPayload;
    private String artifactReferenceJson;
    private AiUsageSnapshot usage;
    private String errorType;
    private String errorMessage;
    private String failureStage;
    private boolean fallbackUsed;

    public boolean isCompleted() {
        return "completed".equals(eventType);
    }

    public boolean isError() {
        return "error".equals(eventType);
    }

    public AiInvokeResult toInvokeResult() {
        AiInvokeResult result = new AiInvokeResult();
        result.setRequestId(requestId);
        result.setTraceId(traceId);
        result.setStatus(status);
        result.setCapability(null);
        result.setResultFormat(resultFormat);
        result.setResultPayload(resultPayload);
        result.setArtifactReferenceJson(artifactReferenceJson);
        result.setUsage(AiUsageSnapshot.orEmpty(usage));
        result.setErrorType(errorType);
        result.setErrorMessage(errorMessage);
        result.setFailureStage(failureStage);
        result.setFallbackUsed(fallbackUsed);
        result.setStreamCompleted(isCompleted());
        return result;
    }
}
