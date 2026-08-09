package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;

public record RejectAiCandidateCommand(AiCandidateId candidateId, String errorType, String errorMessage) {}
