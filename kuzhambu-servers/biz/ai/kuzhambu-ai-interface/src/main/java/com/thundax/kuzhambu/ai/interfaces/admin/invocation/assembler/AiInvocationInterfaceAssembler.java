package com.thundax.kuzhambu.ai.interfaces.admin.invocation.assembler;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.ApplyAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobFailureCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RejectAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.CanDispatchNextAiBatchUnitQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.GetAiBatchJobQuery;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiPromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

public final class AiInvocationInterfaceAssembler {

    private AiInvocationInterfaceAssembler() {}

    @NonNull
    public static AiBatchJobCreateCommand toCreateCommand(@NonNull AiInvocationRequests.BatchCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new AiBatchJobCreateCommand(
                request.getScope(),
                AiBusinessCapability.from(request.getCapability()),
                AiContentRef.ofNullable(request.getContentType(), null),
                request.getTotalCount(),
                request.getFailureSummaryJson());
    }

    @NonNull
    public static RejectAiCandidateCommand toRejectCommand(
            @NonNull AiInvocationRequests.CandidateRejectRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RejectAiCandidateCommand(
                AiCandidateIdCodec.toDomain(request.getCandidateId()),
                request.getErrorType(),
                request.getErrorMessage());
    }

    @NonNull
    public static ApplyAiCandidateCommand toMarkAppliedCommand(
            @NonNull AiInvocationRequests.CandidateMarkAppliedRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ApplyAiCandidateCommand(
                AiCandidateIdCodec.toDomain(request.getCandidateId()),
                request.getResultFormat(),
                request.getResultPayload(),
                null);
    }

    @NonNull
    public static GetAiBatchJobQuery toGetBatchQuery(@NonNull AiInvocationRequests.BatchIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GetAiBatchJobQuery(AiBatchJobIdCodec.toDomain(request.getBatchId()));
    }

    @NonNull
    public static CancelAiBatchJobCommand toCancelBatchCommand(@NonNull AiInvocationRequests.BatchIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new CancelAiBatchJobCommand(AiBatchJobIdCodec.toDomain(request.getBatchId()));
    }

    @NonNull
    public static RecordAiBatchJobCommand toRecordBatchSuccessCommand(
            @NonNull AiInvocationRequests.BatchIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RecordAiBatchJobCommand(AiBatchJobIdCodec.toDomain(request.getBatchId()));
    }

    @NonNull
    public static RecordAiBatchJobFailureCommand toRecordBatchFailureCommand(
            @NonNull AiInvocationRequests.BatchFailureRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RecordAiBatchJobFailureCommand(
                AiBatchJobIdCodec.toDomain(request.getBatchId()), request.getFailureSummaryJson());
    }

    @NonNull
    public static CanDispatchNextAiBatchUnitQuery toCanDispatchBatchQuery(
            @NonNull AiInvocationRequests.BatchIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new CanDispatchNextAiBatchUnitQuery(AiBatchJobIdCodec.toDomain(request.getBatchId()));
    }

    @NonNull
    public static AiInvocationResponses.InvocationLogResponse toResponse(@NonNull AiInvocationLog invocationLog) {
        Objects.requireNonNull(invocationLog, "invocationLog must not be null");
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

    @NonNull
    public static AiInvocationResponses.InvocationSummaryResponse toSummaryResponse(
            @NonNull AiInvocationRequests.InvocationSummaryRequest request, @NonNull List<AiInvocationLog> records) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(records, "records must not be null");
        List<AiInvocationLog> safeRecords = records;
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
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .invocationCount(invocationCount)
                .succeededInvocationCount(succeededInvocationCount)
                .failedInvocationCount(failedInvocationCount)
                .avgLatencyMs(avgLatencyMs)
                .totalCostAmount(totalCostAmount)
                .topCapabilities(topCapabilities)
                .build();
    }

    @NonNull
    public static AiInvocationResponses.CandidateResponse toResponse(@NonNull AiCandidate candidate) {
        Objects.requireNonNull(candidate, "candidate must not be null");
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

    @NonNull
    public static AiInvocationResponses.BatchJobResponse toResponse(@NonNull AiBatchJobResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return AiInvocationResponses.BatchJobResponse.builder()
                .batchId(AiBatchJobIdCodec.toValue(result.getBatchId()))
                .scope(result.getScope())
                .capability(
                        result.getCapability() == null
                                ? null
                                : result.getCapability().value())
                .contentType(AiContentRefCodec.toContentType(result.getContentRef()))
                .contentId(AiContentRefCodec.toContentId(result.getContentRef()))
                .status(result.getStatus() == null ? null : result.getStatus().name())
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

    private static BigDecimal cost(AiInvocationLog invocationLog) {
        return AiUsageSnapshot.orEmpty(invocationLog.getUsage()).getCostAmount();
    }

    private static String longText(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
