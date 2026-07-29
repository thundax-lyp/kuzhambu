package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RejectAiCandidateCommand {

    private final AiCandidateId candidateId;
    private final String errorType;
    private final String errorMessage;
}
