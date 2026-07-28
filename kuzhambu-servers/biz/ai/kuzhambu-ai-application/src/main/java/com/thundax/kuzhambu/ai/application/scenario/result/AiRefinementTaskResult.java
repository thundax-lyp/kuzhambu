package com.thundax.kuzhambu.ai.application.scenario.result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.invocation.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiPromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiRefinementTaskResult {

    private static final String CONTENT_TYPE_SANCAI_ENTRY = "SANCAI_ENTRY";
    private static final String CAPABILITY_IMAGE_ANALYSIS = "classics_image_describe";
    private static final String CAPABILITY_IMAGE_GEN = "classics_image_generate";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, String>> FAILURE_SUMMARY_TYPE = new TypeReference<>() {};

    private final Long taskId;
    private final String scope;
    private final String capability;
    private final String contentType;
    private final Long contentId;
    private final Long objectId;
    private final String requestId;
    private final String traceId;
    private final String status;
    private final String serviceRole;
    private final Long modelId;
    private final String modelName;
    private final Long promptVersionId;
    private final Long callId;
    private final Long candidateId;
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
                contentType(job, invocationLog, candidate),
                contentId(job, invocationLog, candidate),
                objectId(invocationLog, candidate),
                RequestIdCodec.toValue(invocationLog == null ? null : invocationLog.getRequestId()),
                TraceIdCodec.toValue(invocationLog == null ? null : invocationLog.getTraceId()),
                job.getStatus(),
                invocationLog == null ? null : invocationLog.getServiceRole(),
                AiModelIdCodec.toValue(invocationLog == null ? null : invocationLog.getModelId()),
                AiModelNameCodec.toValue(modelName(invocationLog, candidate)),
                promptVersionId(invocationLog, candidate),
                AiCallIdCodec.toValue(callId(invocationLog, candidate)),
                AiCandidateIdCodec.toValue(candidate == null ? null : candidate.getId()),
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
        if (job == null || !CONTENT_TYPE_SANCAI_ENTRY.equals(job.getContentType())) {
            return false;
        }
        return CAPABILITY_IMAGE_ANALYSIS.equals(job.getCapability())
                || CAPABILITY_IMAGE_GEN.equals(job.getCapability());
    }

    private static String contentType(AiBatchJobResult job, AiInvocationLog invocationLog, AiCandidate candidate) {
        String invocationContentType =
                AiContentRefCodec.toContentType(invocationLog == null ? null : invocationLog.getContentRef());
        if (invocationContentType != null) {
            return invocationContentType;
        }
        String candidateContentType =
                AiContentRefCodec.toContentType(candidate == null ? null : candidate.getContentRef());
        return candidateContentType == null ? job.getContentType() : candidateContentType;
    }

    private static Long contentId(AiBatchJobResult job, AiInvocationLog invocationLog, AiCandidate candidate) {
        Long invocationContentId =
                AiContentRefCodec.toContentId(invocationLog == null ? null : invocationLog.getContentRef());
        return invocationContentId == null ? contentId(job, candidate) : invocationContentId;
    }

    private static Long contentId(AiBatchJobResult job, AiCandidate candidate) {
        Long candidateContentId = AiContentRefCodec.toContentId(candidate == null ? null : candidate.getContentRef());
        return candidateContentId == null ? job.getContentId() : candidateContentId;
    }

    private static Long objectId(AiInvocationLog invocationLog, AiCandidate candidate) {
        Long invocationObjectId =
                AiTargetObjectIdCodec.toValue(invocationLog == null ? null : invocationLog.getTargetObjectId());
        return invocationObjectId == null
                ? AiTargetObjectIdCodec.toValue(candidate == null ? null : candidate.getTargetObjectId())
                : invocationObjectId;
    }

    private static AiModelName modelName(AiInvocationLog invocationLog, AiCandidate candidate) {
        return invocationLog == null || invocationLog.getModelName() == null
                ? candidate == null ? null : candidate.getModelName()
                : invocationLog.getModelName();
    }

    private static Long promptVersionId(AiInvocationLog invocationLog, AiCandidate candidate) {
        Long invocationPromptVersionId =
                PromptVersionIdCodec.toValue(invocationLog == null ? null : invocationLog.getPromptVersionId());
        return invocationPromptVersionId == null
                ? AiPromptVersionIdCodec.toValue(candidate == null ? null : candidate.getPromptVersionId())
                : invocationPromptVersionId;
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
