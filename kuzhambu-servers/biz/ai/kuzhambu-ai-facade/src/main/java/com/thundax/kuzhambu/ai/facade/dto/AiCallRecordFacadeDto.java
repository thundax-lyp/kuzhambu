package com.thundax.kuzhambu.ai.facade.dto;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiCallRecordFacadeDto {

    private final Long callId;
    private final Long batchId;
    private final String scope;
    private final String capability;
    private final String contentType;
    private final Long contentId;
    private final Long objectId;
    private final Long serviceId;
    private final String serviceRole;
    private final Long modelId;
    private final String modelName;
    private final Long promptVersionId;
    private final String requestId;
    private final String traceId;
    private final String status;
    private final boolean streamUsed;
    private final boolean streamCompleted;
    private final boolean fallbackUsed;
    private final String errorType;
    private final String errorMessage;
    private final String warningsJson;
    private final Instant requestedAt;
    private final Instant completedAt;
    private final AiUsageSnapshotFacadeDto usage;
}
