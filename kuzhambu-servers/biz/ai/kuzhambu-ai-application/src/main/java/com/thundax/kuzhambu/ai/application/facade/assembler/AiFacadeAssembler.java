package com.thundax.kuzhambu.ai.application.facade.assembler;

import com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult;
import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult.TopCapabilityResult;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiRequest;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiResult;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateApplyCheck;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionRequest;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.facade.dto.AiCallRecordFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiTopCapabilityFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiUsageSnapshotFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiFacadeAssembler {

    public AiCallRecordFacadeDto toFacadeDto(AiCallRecord record) {
        if (record == null) {
            return null;
        }
        return AiCallRecordFacadeDto.builder()
                .callId(record.getCallId())
                .batchId(record.getBatchId())
                .scope(record.getScope())
                .capability(record.getCapability())
                .contentType(record.getContentType())
                .contentId(record.getContentId())
                .objectId(record.getObjectId())
                .serviceId(record.getServiceId())
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
                .errorType(record.getErrorType())
                .errorMessage(record.getErrorMessage())
                .warningsJson(record.getWarningsJson())
                .requestedAt(record.getRequestedAt())
                .completedAt(record.getCompletedAt())
                .usage(toFacadeDto(record.getUsage()))
                .build();
    }

    public AiCandidateFacadeDto toFacadeDto(AiCandidate candidate) {
        if (candidate == null) {
            return null;
        }
        return AiCandidateFacadeDto.builder()
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

    public AiCandidateApplyCheck toDomainCheck(RequirePendingAiCandidateFacadeRequest request) {
        if (request == null) {
            return null;
        }
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(request.getCandidateId());
        check.setContentType(request.getContentType());
        check.setContentId(request.getContentId());
        check.setCapability(request.getCapability());
        return check;
    }

    public Long toCandidateId(MarkAiCandidateAppliedFacadeRequest request) {
        return request == null ? null : request.getCandidateId();
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

    public DiscoveryAiRequest toDomainRequest(DiscoveryAiFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new DiscoveryAiRequest(
                request.getServiceId(),
                request.getServiceRole(),
                request.getModelId(),
                request.getModelName(),
                request.getPromptVersionId(),
                request.getRequestId(),
                request.getTraceId(),
                request.getPromptMessagesJson(),
                request.getPromptVariablesJson(),
                request.getPromptHash(),
                request.getInputPayloadJson(),
                request.getOutputSchemaJson(),
                request.isStream(),
                request.isForceJson(),
                request.getLocale());
    }

    public DiscoveryAiFacadeResponse toFacadeResponse(DiscoveryAiResult result) {
        if (result == null) {
            return null;
        }
        return DiscoveryAiFacadeResponse.builder()
                .callId(result.getCallId())
                .candidateId(result.getCandidateId())
                .status(result.getStatus())
                .capability(result.getCapability())
                .resultFormat(result.getResultFormat())
                .resultPayload(result.getResultPayload())
                .errorType(result.getErrorType())
                .errorMessage(result.getErrorMessage())
                .build();
    }

    public KnowledgeAiExtractionRequest toDomainRequest(KnowledgeAiExtractionFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new KnowledgeAiExtractionRequest(
                request.getTaskType(),
                request.getScopeType(),
                request.getScopeJson(),
                request.getSourceContentType(),
                request.getSourceContentId(),
                request.getRequestedBy(),
                request.getServiceId(),
                request.getServiceRole(),
                request.getModelId(),
                request.getModelName(),
                request.getPromptVersionId(),
                request.getRequestId(),
                request.getTraceId(),
                request.getPromptMessagesJson(),
                request.getPromptVariablesJson(),
                request.getPromptHash(),
                request.getInputPayloadJson(),
                request.getOutputSchemaJson(),
                request.isForceJson(),
                request.getLocale());
    }

    public KnowledgeAiExtractionFacadeResponse toFacadeResponse(KnowledgeAiExtractionResult result) {
        if (result == null) {
            return null;
        }
        return KnowledgeAiExtractionFacadeResponse.builder()
                .callId(result.getCallId())
                .candidateId(result.getCandidateId())
                .status(result.getStatus())
                .capability(result.getCapability())
                .resultFormat(result.getResultFormat())
                .resultPayload(result.getResultPayload())
                .errorType(result.getErrorType())
                .errorMessage(result.getErrorMessage())
                .build();
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

    public AiBatchJobFacadeResponse toFacadeResponse(AiBatchJobResult result) {
        if (result == null) {
            return null;
        }
        return AiBatchJobFacadeResponse.builder()
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

    public AiBatchJobActionFacadeResponse toActionResponse(Long batchId) {
        if (batchId == null) {
            return null;
        }
        return AiBatchJobActionFacadeResponse.builder().batchId(batchId).build();
    }

    private List<AiTopCapabilityFacadeDto> toTopCapabilityFacadeDtos(List<TopCapabilityResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> AiTopCapabilityFacadeDto.builder()
                        .capability(result.getCapability())
                        .invocationCount(result.getInvocationCount())
                        .build())
                .toList();
    }
}
