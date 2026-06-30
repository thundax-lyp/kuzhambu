package com.thundax.kuzhambu.ai.domain.invocation.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiCandidate {

    private Long id;
    private Long candidateId;
    private Long callId;
    private Long batchId;
    private String capability;
    private String contentType;
    private Long contentId;
    private Long objectId;
    private String artifactReferenceJson;
    private String resultFormat;
    private String resultPayload;
    private String status = "PENDING";
    private Long promptVersionId;
    private String modelName;
    private String failureStage;
    private String errorType;
    private String errorMessage;
    private Instant requestedAt;
    private Instant appliedAt;
    private Instant rejectedAt;

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public void reject(String failureType, String failureMessage, String stage, Instant rejectedTime) {
        this.status = "REJECTED";
        this.errorType = failureType;
        this.errorMessage = failureMessage;
        this.failureStage = stage;
        this.rejectedAt = rejectedTime;
    }

    public void reject(String failureType, String failureMessage) {
        reject(failureType, failureMessage, null, Instant.now());
    }

    public void markApplied(Instant appliedTime) {
        this.status = "APPLIED";
        this.appliedAt = appliedTime;
    }
}
