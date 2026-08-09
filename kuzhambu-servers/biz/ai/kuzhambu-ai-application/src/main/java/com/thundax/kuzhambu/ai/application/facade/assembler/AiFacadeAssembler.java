package com.thundax.kuzhambu.ai.application.facade.assembler;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.ApplyAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobFailureCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RejectAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.AiReportSummaryQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.CanDispatchNextAiBatchUnitQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.GetAiBatchJobQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.RequireAiCandidateForApplyQuery;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiReportSummaryResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiReportSummaryResult.TopCapabilityResult;
import com.thundax.kuzhambu.ai.application.scenario.command.DiscoveryAiCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.DiscoveryAiInvokeResult;
import com.thundax.kuzhambu.ai.application.scenario.result.KnowledgeAiExtractionResult;
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
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiReportBucketType;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiInvocationLogFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiTopCapabilityFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiUsageSnapshotFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.AiBatchJobFailureFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.CreateAiBatchJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RejectAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class AiFacadeAssembler {

    public AiInvocationLogFacadeDto toFacadeDto(AiInvocationLog invocationLog) {
        if (invocationLog == null) {
            return null;
        }
        return AiInvocationLogFacadeDto.builder()
                .callId(AiCallIdCodec.toValue(invocationLog.getCallId()))
                .batchId(AiBatchJobIdCodec.toValue(invocationLog.getBatchId()))
                .scope(invocationLog.getScope())
                .capability(
                        invocationLog.getCapability() == null
                                ? null
                                : invocationLog.getCapability().value())
                .contentType(AiContentRefCodec.toContentType(invocationLog.getContentRef()))
                .contentId(AiContentRefCodec.toContentId(invocationLog.getContentRef()))
                .objectId(AiTargetObjectIdCodec.toValue(invocationLog.getTargetObjectId()))
                .serviceId(invocationLog.getServiceId())
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
                .errorType(invocationLog.getErrorType())
                .errorMessage(invocationLog.getErrorMessage())
                .warningsJson(invocationLog.getWarningsJson())
                .requestedAt(invocationLog.getRequestedAt())
                .completedAt(invocationLog.getCompletedAt())
                .usage(toFacadeDto(invocationLog.getUsage()))
                .build();
    }

    public AiCandidateFacadeDto toFacadeDto(AiCandidate candidate) {
        if (candidate == null) {
            return null;
        }
        return AiCandidateFacadeDto.builder()
                .candidateId(AiCandidateIdCodec.toValue(candidate.getId()))
                .callId(AiCallIdCodec.toValue(candidate.getCallId()))
                .batchId(AiBatchJobIdCodec.toValue(candidate.getBatchId()))
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
                .failureStage(candidate.getFailureStage())
                .artifactReferenceJson(candidate.getArtifactReferenceJson())
                .errorType(candidate.getErrorType())
                .errorMessage(candidate.getErrorMessage())
                .requestedAt(candidate.getRequestedAt())
                .appliedAt(candidate.getAppliedAt())
                .rejectedAt(candidate.getRejectedAt())
                .build();
    }

    public AiUsageSnapshotFacadeDto toFacadeDto(AiUsageSnapshot usage) {
        if (usage == null) {
            return null;
        }
        int promptTokens = usage.getInputTokens();
        int completionTokens = usage.getOutputTokens();
        return AiUsageSnapshotFacadeDto.builder()
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(promptTokens + completionTokens)
                .latencyMs(usage.getLatencyMs())
                .costAmount(usage.getCostAmount())
                .currency(null)
                .build();
    }

    public DiscoveryAiCommand toDiscoveryAiCommand(DiscoveryAiFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new DiscoveryAiCommand(
                request.getServiceId(),
                request.getServiceRole(),
                AiModelIdCodec.toDomain(request.getModelId()),
                AiModelNameCodec.toDomain(request.getModelName()),
                PromptVersionIdCodec.toDomain(request.getPromptVersionId()),
                RequestIdCodec.toDomain(request.getRequestId()),
                TraceIdCodec.toDomain(request.getTraceId()),
                request.getPromptMessagesJson(),
                request.getPromptVariablesJson(),
                request.getPromptHash(),
                request.getInputPayloadJson(),
                request.getOutputSchemaJson(),
                request.isStream(),
                request.isForceJson(),
                request.getLocale());
    }

    public DiscoveryAiFacadeResponse toFacadeResponse(DiscoveryAiInvokeResult result) {
        if (result == null) {
            return null;
        }
        return DiscoveryAiFacadeResponse.builder()
                .callId(com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec.toValue(result.getCallId()))
                .candidateId(com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec.toValue(
                        result.getCandidateId()))
                .status(result.getStatus() == null ? null : result.getStatus().name())
                .capability(
                        result.getCapability() == null
                                ? null
                                : result.getCapability().value())
                .resultFormat(result.getResultFormat())
                .resultPayload(result.getResultPayload())
                .errorType(result.getErrorType())
                .errorMessage(result.getErrorMessage())
                .build();
    }

    public KnowledgeAiExtractionCommand toKnowledgeAiExtractionCommand(KnowledgeAiExtractionFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new KnowledgeAiExtractionCommand(
                request.getTaskType(),
                request.getScopeType(),
                request.getScopeJson(),
                request.getSourceContentType(),
                request.getSourceContentId(),
                request.getRequestedBy(),
                request.getServiceId(),
                request.getServiceRole(),
                AiModelIdCodec.toDomain(request.getModelId()),
                AiModelNameCodec.toDomain(request.getModelName()),
                PromptVersionIdCodec.toDomain(request.getPromptVersionId()),
                RequestIdCodec.toDomain(request.getRequestId()),
                TraceIdCodec.toDomain(request.getTraceId()),
                request.getPromptMessagesJson(),
                request.getPromptVariablesJson(),
                request.getPromptHash(),
                request.getInputPayloadJson(),
                request.getOutputSchemaJson(),
                request.isForceJson(),
                request.getLocale());
    }

    public KnowledgeAiExtractionFacadeResponse toFacadeResponse(KnowledgeAiExtractionResult record) {
        if (record == null) {
            return null;
        }
        return KnowledgeAiExtractionFacadeResponse.builder()
                .callId(AiCallIdCodec.toValue(record.getCallId()))
                .candidateId(AiCandidateIdCodec.toValue(record.getCandidateId()))
                .status(record.getStatus() == null ? null : record.getStatus().name())
                .capability(
                        record.getCapability() == null
                                ? null
                                : record.getCapability().value())
                .resultFormat(record.getResultFormat())
                .resultPayload(record.getResultPayload())
                .errorType(record.getErrorType())
                .errorMessage(record.getErrorMessage())
                .build();
    }

    public AiReportSummaryQuery toReportSummaryQuery(AiReportSummaryFacadeRequest request) {
        return new AiReportSummaryQuery(
                request.getPeriodStart(),
                request.getPeriodEnd(),
                request.getBucketType() == null ? null : AiReportBucketType.from(request.getBucketType()));
    }

    public AiReportSummaryFacadeResponse toFacadeResponse(AiReportSummaryResult result) {
        if (result == null) {
            return null;
        }
        return AiReportSummaryFacadeResponse.builder()
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .invocationCount(result.getInvocationCount())
                .succeededInvocationCount(result.getSucceededInvocationCount())
                .failedInvocationCount(result.getFailedInvocationCount())
                .avgLatencyMs(result.getAvgLatencyMs())
                .totalCostAmount(result.getTotalCostAmount())
                .topCapabilities(toTopCapabilityFacadeDtos(result.getTopCapabilities()))
                .build();
    }

    public GetAiBatchJobQuery toGetBatchJobQuery(Long batchId) {
        return new GetAiBatchJobQuery(AiBatchJobIdCodec.toDomain(batchId));
    }

    public AiBatchJobCreateCommand toCreateBatchJobCommand(CreateAiBatchJobFacadeRequest request) {
        return new AiBatchJobCreateCommand(
                request.getScope(),
                AiBusinessCapability.from(request.getCapability()),
                AiContentRef.ofNullable(request.getContentType(), null),
                request.getTotalCount(),
                request.getFailureSummaryJson());
    }

    public CanDispatchNextAiBatchUnitQuery toCanDispatchNextBatchUnitQuery(Long batchId) {
        return new CanDispatchNextAiBatchUnitQuery(AiBatchJobIdCodec.toDomain(batchId));
    }

    public RecordAiBatchJobCommand toRecordBatchJobCommand(Long batchId) {
        return new RecordAiBatchJobCommand(AiBatchJobIdCodec.toDomain(batchId));
    }

    public RecordAiBatchJobFailureCommand toRecordBatchJobFailureCommand(AiBatchJobFailureFacadeRequest request) {
        return new RecordAiBatchJobFailureCommand(
                AiBatchJobIdCodec.toDomain(request.getBatchId()), request.getFailureSummaryJson());
    }

    public CancelAiBatchJobCommand toCancelBatchJobCommand(Long batchId) {
        return new CancelAiBatchJobCommand(AiBatchJobIdCodec.toDomain(batchId));
    }

    public AiBatchJobFacadeResponse toFacadeResponse(AiBatchJobResult result) {
        if (result == null) {
            return null;
        }
        return AiBatchJobFacadeResponse.builder()
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

    public AiBatchJobActionFacadeResponse toActionResponse(Long batchId) {
        if (batchId == null) {
            return null;
        }
        return AiBatchJobActionFacadeResponse.builder().batchId(batchId).build();
    }

    public RequireAiCandidateForApplyQuery toRequirePendingCandidateQuery(
            RequirePendingAiCandidateFacadeRequest request) {
        return new RequireAiCandidateForApplyQuery(
                AiCandidateIdCodec.toDomain(request.getCandidateId()),
                AiContentRef.ofNullable(request.getContentType(), request.getContentId()),
                AiBusinessCapability.from(request.getCapability()),
                AiTargetObjectIdCodec.toDomain(request.getObjectId()));
    }

    public ApplyAiCandidateCommand toApplyCandidateCommand(MarkAiCandidateAppliedFacadeRequest request) {
        return new ApplyAiCandidateCommand(
                AiCandidateIdCodec.toDomain(request.getCandidateId()),
                request.getResultFormat(),
                request.getResultPayload(),
                request.getAppliedAt());
    }

    public RejectAiCandidateCommand toRejectCandidateCommand(RejectAiCandidateFacadeRequest request) {
        return new RejectAiCandidateCommand(
                AiCandidateIdCodec.toDomain(request.getCandidateId()),
                request.getErrorType(),
                request.getErrorMessage());
    }

    private List<AiTopCapabilityFacadeDto> toTopCapabilityFacadeDtos(List<TopCapabilityResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> AiTopCapabilityFacadeDto.builder()
                        .capability(
                                result.getCapability() == null
                                        ? null
                                        : result.getCapability().value())
                        .invocationCount(result.getInvocationCount())
                        .build())
                .toList();
    }
}
