package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import java.time.Instant;

public class ApplyAiCandidateCommand {

    private final AiCandidateId candidateId;
    private final String resultFormat;
    private final String resultPayload;
    private final Instant appliedAt;

    public ApplyAiCandidateCommand(
            AiCandidateId candidateId, String resultFormat, String resultPayload, Instant appliedAt) {
        this.candidateId = candidateId;
        this.resultFormat = resultFormat;
        this.resultPayload = resultPayload;
        this.appliedAt = appliedAt;
    }

    public AiCandidateId getCandidateId() {
        return candidateId;
    }

    public String getResultFormat() {
        return resultFormat;
    }

    public String getResultPayload() {
        return resultPayload;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }
}
