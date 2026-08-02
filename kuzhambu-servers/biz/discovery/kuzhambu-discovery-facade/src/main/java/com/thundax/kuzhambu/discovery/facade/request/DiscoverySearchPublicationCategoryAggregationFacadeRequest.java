package com.thundax.kuzhambu.discovery.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscoverySearchPublicationCategoryAggregationFacadeRequest {

    private final String contentType;
}
