package com.thundax.kuzhambu.ai.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateAiBatchJobFacadeRequest {

    private final String scope;
    private final String capability;
    private final String contentType;
    private final int totalCount;
    private final String failureSummaryJson;
}
