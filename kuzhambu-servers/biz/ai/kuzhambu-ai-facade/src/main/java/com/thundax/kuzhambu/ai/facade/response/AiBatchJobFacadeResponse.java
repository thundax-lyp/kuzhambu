package com.thundax.kuzhambu.ai.facade.response;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiBatchJobFacadeResponse {

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
}
