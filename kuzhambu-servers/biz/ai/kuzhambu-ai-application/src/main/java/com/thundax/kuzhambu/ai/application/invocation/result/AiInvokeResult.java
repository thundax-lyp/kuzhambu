package com.thundax.kuzhambu.ai.application.invocation.result;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
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
public class AiInvokeResult {

    private Long callId;
    private Long candidateId;
    private String requestId;
    private String traceId;
    private String status;
    private String capability;
    private String resultFormat;
    private String resultPayload;
    private AiUsageSnapshot usage = AiUsageSnapshot.empty();
    private String warningsJson;
    private String errorType;
    private String errorMessage;

    public boolean isSucceeded() {
        return "SUCCEEDED".equals(status);
    }

    public AiCandidate toCandidate(AiInvokeCommand command, Long effectiveCallId) {
        AiCandidate candidate = new AiCandidate();
        candidate.setCallId(effectiveCallId);
        candidate.setBatchId(command.getBatchId());
        candidate.setCapability(command.getCapability());
        candidate.setContentType(command.getContentType());
        candidate.setContentId(command.getContentId());
        candidate.setObjectId(command.getObjectId());
        candidate.setResultFormat(resultFormat);
        candidate.setResultPayload(resultPayload);
        candidate.setPromptVersionId(command.getPromptVersionId());
        candidate.setModelName(command.getModelName());
        candidate.setRequestedAt(Instant.now());
        return candidate;
    }

    public static AiInvokeResult failed(String requestId, String traceId, String errorType, String errorMessage) {
        AiInvokeResult result = new AiInvokeResult();
        result.setRequestId(requestId);
        result.setTraceId(traceId);
        result.setStatus("FAILED");
        result.setErrorType(errorType);
        result.setErrorMessage(errorMessage);
        result.setUsage(AiUsageSnapshot.empty());
        return result;
    }
}
