package com.thundax.kuzhambu.ai.interfaces.admin.invocation.assembler;

import com.thundax.kuzhambu.ai.application.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses;
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

    public static AiInvocationResponses.CallRecordResponse toResponse(AiCallRecord record) {
        if (record == null) {
            return AiInvocationResponses.CallRecordResponse.builder().build();
        }
        AiUsageSnapshot usage = AiUsageSnapshot.orEmpty(record.getUsage());
        return AiInvocationResponses.CallRecordResponse.builder()
                .callId(record.getCallId())
                .callIdText(longText(record.getCallId()))
                .batchId(record.getBatchId())
                .scope(record.getScope())
                .capability(record.getCapability())
                .contentType(record.getContentType())
                .contentId(record.getContentId())
                .objectId(record.getObjectId())
                .serviceRole(record.getServiceRole())
                .modelId(record.getModelId())
                .modelName(record.getModelName())
                .promptVersionId(record.getPromptVersionId())
                .requestId(record.getRequestId())
                .traceId(record.getTraceId())
                .status(record.getStatus())
                .streamUsed(record.isStreamUsed())
                .streamCompleted(record.isStreamCompleted())
                .fallbackUsed(record.isFallbackUsed())
                .latencyMs(usage.getLatencyMs())
                .inputTokens(usage.getInputTokens())
                .outputTokens(usage.getOutputTokens())
                .costAmount(usage.getCostAmount())
                .failureStage(record.getFailureStage())
                .resultFormat(record.getResultFormat())
                .errorType(record.getErrorType())
                .errorMessage(record.getErrorMessage())
                .warningsJson(record.getWarningsJson())
                .requestedAt(record.getRequestedAt())
                .completedAt(record.getCompletedAt())
                .build();
    }

    public static AiInvocationResponses.CallSummaryResponse toSummaryResponse(
            Instant periodStart, Instant periodEnd, List<AiCallRecord> records) {
        List<AiCallRecord> safeRecords = records == null ? Collections.emptyList() : records;
        long invocationCount = safeRecords.size();
        long succeededInvocationCount = safeRecords.stream()
                .filter(record -> "SUCCEEDED".equals(record.getStatus()))
                .count();
        long failedInvocationCount = safeRecords.stream()
                .filter(record -> "FAILED".equals(record.getStatus()))
                .count();
        long avgLatencyMs = Math.round(safeRecords.stream()
                .map(AiCallRecord::getUsage)
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
                .collect(Collectors.groupingBy(AiCallRecord::getCapability, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> AiInvocationResponses.TopCapabilityResponse.builder()
                        .capability(entry.getKey())
                        .invocationCount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        return AiInvocationResponses.CallSummaryResponse.builder()
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
        return AiInvocationResponses.CandidateResponse.builder()
                .candidateId(candidate.getCandidateId())
                .candidateIdText(longText(candidate.getCandidateId()))
                .callId(candidate.getCallId())
                .callIdText(longText(candidate.getCallId()))
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

    public static AiInvocationRequests.CandidateMarkAppliedRequest toMarkAppliedRequest(
            Long candidateId, String resultFormat, String resultPayload) {
        AiInvocationRequests.CandidateMarkAppliedRequest request =
                new AiInvocationRequests.CandidateMarkAppliedRequest();
        request.setCandidateId(candidateId);
        request.setResultFormat(resultFormat);
        request.setResultPayload(resultPayload);
        return request;
    }

    public static BigDecimal cost(AiCallRecord record) {
        return AiUsageSnapshot.orEmpty(record == null ? null : record.getUsage())
                .getCostAmount();
    }

    private static String longText(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
