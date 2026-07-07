package com.thundax.kuzhambu.ai.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RejectAiCandidateFacadeRequest {

    private final Long candidateId;
    private final String errorType;
    private final String errorMessage;
}
