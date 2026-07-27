package com.thundax.kuzhambu.ai.domain.batch.model.entity;

import com.thundax.kuzhambu.ai.domain.batch.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.batch.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiBatchJob {

    private AiBatchJobId id;
    private String scope;
    private AiBusinessCapability capability;
    private String contentType;
    private AiBatchJobStatus status = AiBatchJobStatus.RUNNING;
    private int totalCount;
    private int successCount;
    private int failedCount;
    private int cancelledCount;
    private String failureSummaryJson;
    private Instant requestedAt;
    private Instant cancelledAt;
    private Instant completedAt;

    public void recordSuccess() {
        successCount++;
        completeIfFinished();
    }

    public void recordFailure() {
        failedCount++;
        completeIfFinished();
    }

    public void cancel(Instant cancelledTime) {
        status = AiBatchJobStatus.CANCELLED;
        cancelledAt = cancelledTime;
    }

    private void completeIfFinished() {
        if (successCount + failedCount + cancelledCount >= totalCount) {
            status = failedCount == 0 ? AiBatchJobStatus.SUCCEEDED : AiBatchJobStatus.PARTIAL;
            completedAt = Instant.now();
        }
    }
}
