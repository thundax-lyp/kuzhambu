package com.thundax.kuzhambu.ai.application.batch.result;

import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiBatchJobResult {

    private final Long batchId;
    private final String scope;
    private final String capability;
    private final String contentType;
    private final String status;
    private final int totalCount;
    private final int successCount;
    private final int failedCount;
    private final int cancelledCount;
    private final String failureSummaryJson;
    private final Instant requestedAt;
    private final Instant cancelledAt;
    private final Instant completedAt;

    public static AiBatchJobResult from(AiBatchJob job) {
        if (job == null) {
            return null;
        }
        return new AiBatchJobResult(
                job.getBatchId(),
                job.getScope(),
                job.getCapability(),
                job.getContentType(),
                job.getStatus(),
                job.getTotalCount(),
                job.getSuccessCount(),
                job.getFailedCount(),
                job.getCancelledCount(),
                job.getFailureSummaryJson(),
                job.getRequestedAt(),
                job.getCancelledAt(),
                job.getCompletedAt());
    }
}
