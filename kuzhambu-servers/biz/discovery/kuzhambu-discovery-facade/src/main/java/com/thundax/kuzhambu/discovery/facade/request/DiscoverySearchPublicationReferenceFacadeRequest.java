package com.thundax.kuzhambu.discovery.facade.request;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscoverySearchPublicationReferenceFacadeRequest {

    private final String documentId;
    private final String contentVersionId;
    private final Integer contentVersionNo;
    private final Instant occurredAt;
}
