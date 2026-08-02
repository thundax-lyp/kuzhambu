package com.thundax.kuzhambu.discovery.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscoverySearchPublicationCandidateFacadeResponse {

    private final String contentType;
    private final String contentId;
    private final String categoryId;
    private final String volumeId;
}
