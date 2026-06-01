package com.thundax.kuzhambu.ai.interfaces.admin.invocation.assembler;

import com.thundax.kuzhambu.ai.application.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses;
import java.math.BigDecimal;

public final class AiInvocationInterfaceAssembler {

    private AiInvocationInterfaceAssembler() {}

    public static AiBatchJobCreateCommand toCreateCommand(AiInvocationRequests.BatchCreateRequest request) {
        AiBatchJobCreateCommand command = new AiBatchJobCreateCommand();
        command.setScope(request.getScope());
        command.setCapability(request.getCapability());
        command.setContentType(request.getContentType());
        command.setTotalCount(request.getTotalCount());
        command.setFailureSummaryJson(request.getFailureSummaryJson());
        return command;
    }

    public static AiInvocationResponses.CallRecordResponse toResponse(AiCallRecord record) {
        if (record == null) {
            return AiInvocationResponses.CallRecordResponse.builder().build();
        }
        AiUsageSnapshot usage = AiUsageSnapshot.orEmpty(record.getUsage());
        return AiInvocationResponses.CallRecordResponse.builder()
                .callId(record.getCallId())
                .batchId(record.getBatchId())
                .scope(record.getScope())
                .capability(record.getCapability())
                .contentType(record.getContentType())
                .contentId(record.getContentId())
                .objectId(record.getObjectId())
                .modelName(record.getModelName())
                .status(record.getStatus())
                .streamUsed(record.isStreamUsed())
                .streamCompleted(record.isStreamCompleted())
                .fallbackUsed(record.isFallbackUsed())
                .latencyMs(usage.getLatencyMs())
                .inputTokens(usage.getInputTokens())
                .outputTokens(usage.getOutputTokens())
                .costAmount(usage.getCostAmount())
                .errorType(record.getErrorType())
                .errorMessage(record.getErrorMessage())
                .warningsJson(record.getWarningsJson())
                .requestedAt(record.getRequestedAt())
                .completedAt(record.getCompletedAt())
                .build();
    }

    public static AiInvocationResponses.CandidateResponse toResponse(AiCandidate candidate) {
        if (candidate == null) {
            return AiInvocationResponses.CandidateResponse.builder().build();
        }
        return AiInvocationResponses.CandidateResponse.builder()
                .candidateId(candidate.getCandidateId())
                .callId(candidate.getCallId())
                .batchId(candidate.getBatchId())
                .capability(candidate.getCapability())
                .contentType(candidate.getContentType())
                .contentId(candidate.getContentId())
                .objectId(candidate.getObjectId())
                .resultFormat(candidate.getResultFormat())
                .resultPayload(candidate.getResultPayload())
                .status(candidate.getStatus())
                .promptVersionId(candidate.getPromptVersionId())
                .modelName(candidate.getModelName())
                .errorType(candidate.getErrorType())
                .errorMessage(candidate.getErrorMessage())
                .requestedAt(candidate.getRequestedAt())
                .appliedAt(candidate.getAppliedAt())
                .build();
    }

    public static AiInvocationResponses.BatchJobResponse toResponse(AiBatchJobResult result) {
        if (result == null) {
            return AiInvocationResponses.BatchJobResponse.builder().build();
        }
        return AiInvocationResponses.BatchJobResponse.builder()
                .batchId(result.getBatchId())
                .scope(result.getScope())
                .capability(result.getCapability())
                .contentType(result.getContentType())
                .status(result.getStatus())
                .totalCount(result.getTotalCount())
                .successCount(result.getSuccessCount())
                .failedCount(result.getFailedCount())
                .cancelledCount(result.getCancelledCount())
                .failureSummaryJson(result.getFailureSummaryJson())
                .requestedAt(result.getRequestedAt())
                .cancelledAt(result.getCancelledAt())
                .completedAt(result.getCompletedAt())
                .build();
    }

    public static BigDecimal cost(AiCallRecord record) {
        return AiUsageSnapshot.orEmpty(record == null ? null : record.getUsage())
                .getCostAmount();
    }
}
