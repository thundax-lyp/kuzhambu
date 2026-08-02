package com.thundax.kuzhambu.discovery.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscoverySearchPublicationCategoryAggregationFacadeResponse {

    private final String categoryId;
    private final long readyEntryCount;
    private final String representativeContentId;
}
