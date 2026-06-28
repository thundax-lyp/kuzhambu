package com.thundax.kuzhambu.ai.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeAiExtractionFacadeResponse {

    private final Long callId;
    private final Long candidateId;
    private final String status;
    private final String capability;
    private final String resultFormat;
    private final String resultPayload;
    private final String errorType;
    private final String errorMessage;
}
