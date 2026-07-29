package com.thundax.kuzhambu.ai.application.scenario.result;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KnowledgeAiExtractionResult {

    private final AiCallId callId;
    private final AiCandidateId candidateId;
    private final AiInvocationStatus status;
    private final AiBusinessCapability capability;
    private final String resultFormat;
    private final String resultPayload;
    private final String errorType;
    private final String errorMessage;
}
