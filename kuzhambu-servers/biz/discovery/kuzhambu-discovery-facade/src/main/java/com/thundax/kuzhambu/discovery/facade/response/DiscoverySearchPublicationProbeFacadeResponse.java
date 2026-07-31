package com.thundax.kuzhambu.discovery.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscoverySearchPublicationProbeFacadeResponse {

    private final boolean present;
    private final String publicationStatus;
    private final Boolean deleted;
    private final String contentVersionId;
    private final Integer contentVersionNo;
}
