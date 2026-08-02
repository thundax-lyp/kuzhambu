package com.thundax.kuzhambu.discovery.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscoverySearchPublicationCandidatePageFacadeRequest {

    private final String contentType;
    private final String categoryId;
    private final String volumeId;
    private final String keyword;
    private final Integer pageNo;
    private final Integer pageSize;
}
