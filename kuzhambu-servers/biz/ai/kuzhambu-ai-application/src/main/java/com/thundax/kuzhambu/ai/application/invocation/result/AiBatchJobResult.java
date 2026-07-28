package com.thundax.kuzhambu.ai.application.invocation.result;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiBatchJobResult {

    private final AiBatchJobId batchId;
    private final String scope;
    private final AiBusinessCapability capability;
    private final AiContentRef contentRef;
    private final AiBatchJobStatus status;
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
                job.getId(),
                job.getScope(),
                job.getCapability(),
                AiContentRefCodec.toDomain(job.getContentType(), job.getContentId()),
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
