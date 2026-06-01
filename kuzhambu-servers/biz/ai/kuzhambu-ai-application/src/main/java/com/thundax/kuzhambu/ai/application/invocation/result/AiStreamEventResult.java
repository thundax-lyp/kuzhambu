package com.thundax.kuzhambu.ai.application.invocation.result;

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
public class AiStreamEventResult {

    private String eventType;
    private String eventId;
    private String requestId;
    private String traceId;
    private String stage;
    private Instant timestamp;
    private String deltaText;
    private String status;
    private String resultFormat;
    private String resultPayload;
    private AiUsageSnapshot usage;
    private String errorType;
    private String errorMessage;

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
        result.setUsage(AiUsageSnapshot.orEmpty(usage));
        result.setErrorType(errorType);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
