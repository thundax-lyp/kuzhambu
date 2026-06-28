package com.thundax.kuzhambu.ai.application.facade.assembler;

import com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult;
import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult.TopCapabilityResult;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiRequest;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiResult;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionRequest;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.facade.dto.AiTopCapabilityFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
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
