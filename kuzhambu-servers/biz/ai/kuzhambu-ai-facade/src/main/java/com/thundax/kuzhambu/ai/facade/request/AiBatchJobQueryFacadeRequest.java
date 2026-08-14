package com.thundax.kuzhambu.ai.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiBatchJobQueryFacadeRequest {

    private final String scope;
    private final String capability;
    private final String status;
    private final String contentType;
    private final Long contentId;
    private final int pageNo;
    private final int pageSize;
}
