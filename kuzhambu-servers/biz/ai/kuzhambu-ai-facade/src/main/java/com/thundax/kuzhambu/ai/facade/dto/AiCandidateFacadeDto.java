package com.thundax.kuzhambu.ai.facade.dto;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiCandidateFacadeDto {

    private final Long candidateId;
    private final Long callId;
    private final Long batchId;
    private final String capability;
    private final String contentType;
    private final Long contentId;
    private final Long objectId;
    private final String resultFormat;
    private final String resultPayload;
    private final String status;
    private final Long promptVersionId;
    private final String modelName;
    private final String failureStage;
    private final String artifactReferenceJson;
    private final String errorType;
    private final String errorMessage;
    private final Instant requestedAt;
    private final Instant appliedAt;
    private final Instant rejectedAt;
}
