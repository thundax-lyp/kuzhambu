package com.thundax.kuzhambu.ai.application.scenario.result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiPromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiRefinementTaskResult {

    private static final String CONTENT_TYPE_SANCAI_ENTRY = "SANCAI_ENTRY";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, String>> FAILURE_SUMMARY_TYPE = new TypeReference<>() {};

    private final AiBatchJobId taskId;
    private final String scope;
    private final AiBusinessCapability capability;
    private final AiContentRef contentRef;
    private final AiTargetObjectId targetObjectId;
    private final RequestId requestId;
    private final TraceId traceId;
    private final AiBatchJobStatus status;
    private final String serviceRole;
    private final AiModelId modelId;
    private final AiModelName modelName;
    private final PromptVersionId promptVersionId;
    private final AiCallId callId;
    private final AiCandidateId candidateId;
    private final String resultFormat;
    private final String resultPreview;
    private final String failureStage;
    private final String errorType;
    private final String errorMessage;
    private final boolean streamEnabled;
    private final Instant requestedAt;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Instant cancelledAt;

    public static AiRefinementTaskResult fromBatchJob(AiBatchJobResult job) {
        return fromBatchJob(job, null, null);
    }

    public static AiRefinementTaskResult fromBatchJob(
            AiBatchJobResult job, AiInvocationLog invocationLog, AiCandidate candidate) {
        if (job == null) {
            return null;
        }
        FailureSummary failureSummary = FailureSummary.from(job.getFailureSummaryJson());
        return new AiRefinementTaskResult(
                job.getBatchId(),
                job.getScope(),
                job.getCapability(),
                contentRef(job, invocationLog, candidate),
                targetObjectId(invocationLog, candidate),
                invocationLog == null ? null : invocationLog.getRequestId(),
                invocationLog == null ? null : invocationLog.getTraceId(),
                job.getStatus(),
                invocationLog == null ? null : invocationLog.getServiceRole(),
                invocationLog == null ? null : invocationLog.getModelId(),
                modelName(invocationLog, candidate),
                promptVersionId(invocationLog, candidate),
                callId(invocationLog, candidate),
                candidate == null ? null : candidate.getId(),
                resultFormat(invocationLog, candidate),
                resultPayload(invocationLog, candidate),
                failureStage(invocationLog, candidate, failureSummary),
                errorType(invocationLog, candidate, failureSummary),
                errorMessage(job, invocationLog, candidate, failureSummary),
                isStreamEnabled(job),
                job.getRequestedAt(),
                invocationLog == null ? null : invocationLog.getRequestedAt(),
                job.getCompletedAt(),
                job.getCancelledAt());
    }

    private static boolean isStreamEnabled(AiBatchJobResult job) {
        if (job == null || !CONTENT_TYPE_SANCAI_ENTRY.equals(AiContentRefCodec.toContentType(job.getContentRef()))) {
            return false;
        }
        return AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE == job.getCapability()
                || AiBusinessCapability.CLASSICS_IMAGE_GENERATE == job.getCapability();
    }

    private static AiContentRef contentRef(AiBatchJobResult job, AiInvocationLog invocationLog, AiCandidate candidate) {
        if (invocationLog != null && invocationLog.getContentRef() != null) {
            return invocationLog.getContentRef();
        }
        return candidate == null || candidate.getContentRef() == null ? job.getContentRef() : candidate.getContentRef();
    }

    private static AiTargetObjectId targetObjectId(AiInvocationLog invocationLog, AiCandidate candidate) {
        return invocationLog == null || invocationLog.getTargetObjectId() == null
                ? candidate == null ? null : candidate.getTargetObjectId()
                : invocationLog.getTargetObjectId();
    }

    private static AiModelName modelName(AiInvocationLog invocationLog, AiCandidate candidate) {
        return invocationLog == null || invocationLog.getModelName() == null
                ? candidate == null ? null : candidate.getModelName()
                : invocationLog.getModelName();
    }

    private static PromptVersionId promptVersionId(AiInvocationLog invocationLog, AiCandidate candidate) {
        if (invocationLog != null && invocationLog.getPromptVersionId() != null) {
            return invocationLog.getPromptVersionId();
        }
        return PromptVersionIdCodec.toDomain(
                AiPromptVersionIdCodec.toValue(candidate == null ? null : candidate.getPromptVersionId()));
    }

    private static AiCallId callId(AiInvocationLog invocationLog, AiCandidate candidate) {
        return invocationLog == null || invocationLog.getCallId() == null
                ? candidate == null ? null : candidate.getCallId()
                : invocationLog.getCallId();
    }

    private static String resultFormat(AiInvocationLog invocationLog, AiCandidate candidate) {
        if (candidate != null && candidate.getResultFormat() != null) {
            return candidate.getResultFormat();
        }
        return invocationLog == null ? null : invocationLog.getResultFormat();
    }

    private static String resultPayload(AiInvocationLog invocationLog, AiCandidate candidate) {
        if (candidate != null && candidate.getResultPayload() != null) {
            return candidate.getResultPayload();
        }
        return invocationLog == null ? null : invocationLog.getResultPayload();
    }

    private static String failureStage(
            AiInvocationLog invocationLog, AiCandidate candidate, FailureSummary failureSummary) {
        if (candidate != null && candidate.getFailureStage() != null) {
            return candidate.getFailureStage();
        }
        if (invocationLog != null && invocationLog.getFailureStage() != null) {
            return invocationLog.getFailureStage();
        }
        return failureSummary.failureStage;
    }

    private static String errorType(
            AiInvocationLog invocationLog, AiCandidate candidate, FailureSummary failureSummary) {
        if (candidate != null && candidate.getErrorType() != null) {
            return candidate.getErrorType();
        }
        if (invocationLog != null && invocationLog.getErrorType() != null) {
            return invocationLog.getErrorType();
        }
        return failureSummary.errorType;
    }

    private static String errorMessage(
            AiBatchJobResult job, AiInvocationLog invocationLog, AiCandidate candidate, FailureSummary failureSummary) {
        if (candidate != null && candidate.getErrorMessage() != null) {
            return candidate.getErrorMessage();
        }
        if (invocationLog != null && invocationLog.getErrorMessage() != null) {
            return invocationLog.getErrorMessage();
        }
        return failureSummary.errorMessage == null ? job.getFailureSummaryJson() : failureSummary.errorMessage;
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value;
    }

    private static final class FailureSummary {
        private final String failureStage;
        private final String errorType;
        private final String errorMessage;

        private FailureSummary(String failureStage, String errorType, String errorMessage) {
            this.failureStage = failureStage;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
        }

        private static FailureSummary from(String failureSummaryJson) {
            if (blankToNull(failureSummaryJson) == null) {
                return new FailureSummary(null, null, null);
            }
            try {
                Map<String, String> fields = OBJECT_MAPPER.readValue(failureSummaryJson, FAILURE_SUMMARY_TYPE);
                return new FailureSummary(
                        blankToNull(fields.get("failureStage")),
                        blankToNull(fields.get("errorType")),
                        blankToNull(fields.get("errorMessage")));
            } catch (Exception exception) {
                return new FailureSummary(null, null, null);
            }
        }
    }
}
