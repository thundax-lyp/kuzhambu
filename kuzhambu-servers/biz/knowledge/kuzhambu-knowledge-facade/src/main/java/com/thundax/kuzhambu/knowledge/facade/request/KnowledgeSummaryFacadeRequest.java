package com.thundax.kuzhambu.knowledge.facade.request;

import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeSummaryFacadeRequest {

    private final Date periodStart;
    private final Date periodEnd;
    private final String bucketType;
}
