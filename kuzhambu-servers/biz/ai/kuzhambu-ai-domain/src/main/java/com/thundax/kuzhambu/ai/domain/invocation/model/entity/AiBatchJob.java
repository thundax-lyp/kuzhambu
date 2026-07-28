package com.thundax.kuzhambu.ai.domain.invocation.model.entity;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
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
    private Long contentId;
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

    public void recordPartial() {
        successCount++;
        if (successCount + failedCount + cancelledCount >= totalCount) {
            status = AiBatchJobStatus.PARTIAL;
            completedAt = Instant.now();
        }
    }

    public void cancel(Instant cancelledTime) {
        status = AiBatchJobStatus.CANCELLED;
        cancelledAt = cancelledTime;
    }

    private void completeIfFinished() {
        if (successCount + failedCount + cancelledCount >= totalCount) {
            if (failedCount == 0) {
                status = AiBatchJobStatus.SUCCEEDED;
            } else if (successCount == 0) {
                status = AiBatchJobStatus.FAILED;
            } else {
                status = AiBatchJobStatus.PARTIAL;
            }
            completedAt = Instant.now();
        }
    }
}
