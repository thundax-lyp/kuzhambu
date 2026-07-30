package com.thundax.kuzhambu.knowledge.facade.request;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeSummaryFacadeRequest {

    private final Instant periodStart;
    private final Instant periodEnd;
    private final String bucketType;
}
