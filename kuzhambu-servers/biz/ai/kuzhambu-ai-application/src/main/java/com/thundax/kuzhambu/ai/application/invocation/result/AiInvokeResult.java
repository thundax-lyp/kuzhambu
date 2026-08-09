package com.thundax.kuzhambu.ai.application.invocation.result;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiPromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
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
public class AiInvokeResult {

    private AiCallId callId;
    private AiCandidateId candidateId;
    private RequestId requestId;
    private TraceId traceId;
    private AiInvocationStatus status;
    private AiBusinessCapability capability;
    private String resultFormat;
    private String resultPayload;
    private String artifactReferenceJson;
    private AiUsageSnapshot usage = AiUsageSnapshot.empty();
    private String warningsJson;
    private String errorType;
    private String errorMessage;
    private String failureStage;
    private boolean streamCompleted;
    private boolean fallbackUsed;

    public boolean isSucceeded() {
        return AiInvocationStatus.SUCCEEDED == status;
    }

    public AiCandidate toCandidate(AiInvokeCommand command, AiCallId effectiveCallId) {
        AiCandidate candidate = new AiCandidate();
        candidate.setCallId(effectiveCallId);
        candidate.setBatchId(command.batchId());
        candidate.setCapability(command.capability());
        candidate.setContentRef(command.contentRef());
        candidate.setTargetObjectId(command.targetObjectId());
        candidate.setArtifactReferenceJson(artifactReferenceJson);
        candidate.setResultFormat(resultFormat);
        candidate.setResultPayload(resultPayload);
        candidate.setFailureStage(failureStage);
        candidate.setErrorType(errorType);
        candidate.setErrorMessage(errorMessage);
        candidate.setPromptVersionId(AiPromptVersionIdCodec.toDomain(
                command.promptVersionId() == null
                        ? null
                        : command.promptVersionId().value()));
        candidate.setModelName(command.modelName());
        candidate.setRequestedAt(Instant.now());
        return candidate;
    }

    public static AiInvokeResult failed(
            RequestId requestId, TraceId traceId, String errorType, String errorMessage, String failureStage) {
        AiInvokeResult result = new AiInvokeResult();
        result.setRequestId(requestId);
        result.setTraceId(traceId);
        result.setStatus(AiInvocationStatus.FAILED);
        result.setErrorType(errorType);
        result.setErrorMessage(errorMessage);
        result.setFailureStage(failureStage);
        result.setUsage(AiUsageSnapshot.empty());
        return result;
    }
}
