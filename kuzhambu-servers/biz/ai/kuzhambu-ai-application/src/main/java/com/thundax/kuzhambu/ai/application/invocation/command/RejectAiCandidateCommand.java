package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;

public class RejectAiCandidateCommand {

    private final AiCandidateId candidateId;
    private final String errorType;
    private final String errorMessage;

    public RejectAiCandidateCommand(AiCandidateId candidateId, String errorType, String errorMessage) {
        this.candidateId = candidateId;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public AiCandidateId getCandidateId() {
        return candidateId;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
