package com.thundax.kuzhambu.ai.facade.request;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MarkAiCandidateAppliedFacadeRequest {

    private final Long candidateId;
    private final String resultFormat;
    private final String resultPayload;
    private final Instant appliedAt;
}
