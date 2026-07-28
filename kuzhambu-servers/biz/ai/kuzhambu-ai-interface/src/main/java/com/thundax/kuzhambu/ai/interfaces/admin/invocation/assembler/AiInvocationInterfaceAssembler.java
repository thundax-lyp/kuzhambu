package com.thundax.kuzhambu.ai.interfaces.admin.invocation.assembler;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiPromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public static AiInvocationResponses.InvocationLogResponse toResponse(AiInvocationLog invocationLog) {
        if (invocationLog == null) {
            return AiInvocationResponses.InvocationLogResponse.builder().build();
        }
        AiUsageSnapshot usage = AiUsageSnapshot.orEmpty(invocationLog.getUsage());
        Long callId = AiCallIdCodec.toValue(invocationLog.getCallId());
        return AiInvocationResponses.InvocationLogResponse.builder()
                .callId(callId)
                .callIdText(longText(callId))
                .batchId(AiBatchJobIdCodec.toValue(invocationLog.getBatchId()))
                .scope(invocationLog.getScope())
                .capability(
                        invocationLog.getCapability() == null
                                ? null
                                : invocationLog.getCapability().value())
                .contentType(AiContentRefCodec.toContentType(invocationLog.getContentRef()))
                .contentId(AiContentRefCodec.toContentId(invocationLog.getContentRef()))
                .objectId(AiTargetObjectIdCodec.toValue(invocationLog.getTargetObjectId()))
                .serviceRole(invocationLog.getServiceRole())
                .modelId(AiModelIdCodec.toValue(invocationLog.getModelId()))
                .modelName(AiModelNameCodec.toValue(invocationLog.getModelName()))
                .promptVersionId(PromptVersionIdCodec.toValue(invocationLog.getPromptVersionId()))
                .requestId(RequestIdCodec.toValue(invocationLog.getRequestId()))
                .traceId(TraceIdCodec.toValue(invocationLog.getTraceId()))
                .status(
                        invocationLog.getStatus() == null
                                ? null
                                : invocationLog.getStatus().name())
                .streamUsed(invocationLog.isStreamUsed())
                .streamCompleted(invocationLog.isStreamCompleted())
                .fallbackUsed(invocationLog.isFallbackUsed())
                .latencyMs(usage.getLatencyMs())
                .inputTokens(usage.getInputTokens())
                .outputTokens(usage.getOutputTokens())
                .costAmount(usage.getCostAmount())
                .failureStage(invocationLog.getFailureStage())
                .resultFormat(invocationLog.getResultFormat())
                .errorType(invocationLog.getErrorType())
                .errorMessage(invocationLog.getErrorMessage())
                .warningsJson(invocationLog.getWarningsJson())
                .requestedAt(invocationLog.getRequestedAt())
                .completedAt(invocationLog.getCompletedAt())
                .build();
    }

    public static AiInvocationResponses.InvocationSummaryResponse toSummaryResponse(
            Instant periodStart, Instant periodEnd, List<AiInvocationLog> records) {
        List<AiInvocationLog> safeRecords = records == null ? Collections.emptyList() : records;
        long invocationCount = safeRecords.size();
        long succeededInvocationCount = safeRecords.stream()
                .filter(record -> AiInvocationStatus.SUCCEEDED == record.getStatus())
                .count();
        long failedInvocationCount = safeRecords.stream()
                .filter(record -> AiInvocationStatus.FAILED == record.getStatus())
                .count();
        long avgLatencyMs = Math.round(safeRecords.stream()
                .map(AiInvocationLog::getUsage)
                .map(AiUsageSnapshot::orEmpty)
                .map(AiUsageSnapshot::getLatencyMs)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0));
        BigDecimal totalCostAmount =
                safeRecords.stream().map(AiInvocationInterfaceAssembler::cost).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<AiInvocationResponses.TopCapabilityResponse> topCapabilities = safeRecords.stream()
                .filter(record -> record.getCapability() != null)
                .collect(Collectors.groupingBy(record -> record.getCapability().value(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> AiInvocationResponses.TopCapabilityResponse.builder()
                        .capability(entry.getKey())
                        .invocationCount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        return AiInvocationResponses.InvocationSummaryResponse.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .invocationCount(invocationCount)
                .succeededInvocationCount(succeededInvocationCount)
                .failedInvocationCount(failedInvocationCount)
                .avgLatencyMs(avgLatencyMs)
                .totalCostAmount(totalCostAmount)
                .topCapabilities(topCapabilities)
                .build();
    }

    public static AiInvocationResponses.CandidateResponse toResponse(AiCandidate candidate) {
        if (candidate == null) {
            return AiInvocationResponses.CandidateResponse.builder().build();
        }
        Long candidateId = AiCandidateIdCodec.toValue(candidate.getId());
        Long callId = AiCallIdCodec.toValue(candidate.getCallId());
        Long batchId = AiBatchJobIdCodec.toValue(candidate.getBatchId());
        return AiInvocationResponses.CandidateResponse.builder()
                .candidateId(candidateId)
                .candidateIdText(longText(candidateId))
                .callId(callId)
                .callIdText(longText(callId))
                .batchId(batchId)
                .capability(
                        candidate.getCapability() == null
                                ? null
                                : candidate.getCapability().value())
                .contentType(AiContentRefCodec.toContentType(candidate.getContentRef()))
                .contentId(AiContentRefCodec.toContentId(candidate.getContentRef()))
                .objectId(AiTargetObjectIdCodec.toValue(candidate.getTargetObjectId()))
                .resultFormat(candidate.getResultFormat())
                .resultPayload(candidate.getResultPayload())
                .status(
                        candidate.getStatus() == null
                                ? null
                                : candidate.getStatus().name())
                .promptVersionId(AiPromptVersionIdCodec.toValue(candidate.getPromptVersionId()))
                .modelName(AiModelNameCodec.toValue(candidate.getModelName()))
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
                .contentId(result.getContentId())
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

    public static AiInvocationRequests.CandidateMarkAppliedRequest toMarkAppliedRequest(
            Long candidateId, String resultFormat, String resultPayload) {
        AiInvocationRequests.CandidateMarkAppliedRequest request =
                new AiInvocationRequests.CandidateMarkAppliedRequest();
        request.setCandidateId(candidateId);
        request.setResultFormat(resultFormat);
        request.setResultPayload(resultPayload);
        return request;
    }

    public static BigDecimal cost(AiInvocationLog invocationLog) {
        return AiUsageSnapshot.orEmpty(invocationLog == null ? null : invocationLog.getUsage())
                .getCostAmount();
    }

    private static String longText(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
