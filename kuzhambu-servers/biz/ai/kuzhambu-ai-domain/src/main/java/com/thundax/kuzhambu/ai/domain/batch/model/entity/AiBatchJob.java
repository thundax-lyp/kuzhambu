package com.thundax.kuzhambu.ai.domain.batch.model.entity;

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

    private Long id;
    private Long batchId;
    private String scope;
    private String capability;
    private String contentType;
    private String status = "RUNNING";
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
        status = "CANCELLED";
        cancelledAt = cancelledTime;
    }

    private void completeIfFinished() {
        if (successCount + failedCount + cancelledCount >= totalCount) {
            status = failedCount == 0 ? "SUCCEEDED" : "PARTIAL";
            completedAt = Instant.now();
        }
    }
}
