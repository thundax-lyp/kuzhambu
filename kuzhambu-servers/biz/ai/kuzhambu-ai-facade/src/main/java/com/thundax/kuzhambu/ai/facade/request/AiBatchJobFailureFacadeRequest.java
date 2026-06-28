package com.thundax.kuzhambu.ai.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiBatchJobFailureFacadeRequest {

    private final Long batchId;
    private final String failureSummaryJson;
}
