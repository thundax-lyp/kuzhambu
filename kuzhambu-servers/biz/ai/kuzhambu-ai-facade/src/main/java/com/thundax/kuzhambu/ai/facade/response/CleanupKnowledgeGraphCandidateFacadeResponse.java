package com.thundax.kuzhambu.ai.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CleanupKnowledgeGraphCandidateFacadeResponse {
    private final Long candidateId;
    private final boolean cleaned;
}
