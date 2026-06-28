package com.thundax.kuzhambu.ai.application.facade.assembler;

import com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult;
import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult.TopCapabilityResult;
import com.thundax.kuzhambu.ai.facade.dto.AiTopCapabilityFacadeDto;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiFacadeAssembler {

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
